package com.library.gateway.service

import com.library.gateway.config.DEFAULT_LANGUAGE
import com.library.gateway.config.SYSTEM_ACCOUNT
import com.library.gateway.domain.Authority
import com.library.gateway.domain.User
import com.library.gateway.repository.AuthorityRepository
import com.library.gateway.repository.UserRepository
import com.library.gateway.security.getCurrentUserLogin
import com.library.gateway.service.dto.AdminUserDTO
import com.library.gateway.service.dto.UserDTO
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Pageable
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Service class for managing users.
 */
@Service
class UserService(
    private val userRepository: UserRepository,
    private val authorityRepository: AuthorityRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)
    /**
     * Update basic information (first name, last name, email, language) for the current user.
     *
     * @param firstName first name of user.
     * @param lastName  last name of user.
     * @param email     email id of user.
     * @param langKey   language key.
     * @param imageUrl  image URL of user.
     * @return a completed {@link Mono}.
     */
    @Transactional
    fun updateUser(firstName: String?, lastName: String?, email: String?, langKey: String?, imageUrl: String?): Mono<Void> {
        return getCurrentUserLogin()
            .flatMap(userRepository::findOneByLogin)
            .flatMap {

                it.firstName = firstName
                it.lastName = lastName
                it.email = email?.toLowerCase()
                it.langKey = langKey
                it.imageUrl = imageUrl
                saveUser(it)
            }
            .doOnNext { log.debug("Changed Information for User: $it") }
            .then()
    }

    @Transactional
    fun saveUser(user: User) = saveUser(user, false)

    @Transactional
    fun saveUser(user: User, forceCreate: Boolean): Mono<User> {
        return getCurrentUserLogin()
            .switchIfEmpty(Mono.just(SYSTEM_ACCOUNT))
            .flatMap { login ->
                if (user.createdBy == null) {
                    user.createdBy = login
                }
                user.lastModifiedBy = login
                // Saving the relationship can be done in an entity callback
                // once https://github.com/spring-projects/spring-data-r2dbc/issues/215 is done
                val persistedUser = if (forceCreate) { userRepository.create(user) } else { userRepository.save(user) }
                persistedUser
                    .flatMap { user ->
                        Flux.fromIterable(user.authorities)
                            .flatMap { user.id?.let { it1 -> it.name?.let { it2 -> userRepository.saveUserAuthority(it1, it2) } } }
                            .then(Mono.just(user))
                    }
            }
    }

    @Transactional(readOnly = true)
    fun getAllManagedUsers(pageable: Pageable): Flux<AdminUserDTO> {
        return userRepository.findAllWithAuthorities(pageable).map { AdminUserDTO(it) }
    }

    @Transactional(readOnly = true)
    fun getAllPublicUsers(pageable: Pageable): Flux<UserDTO> {
        return userRepository.findAllByIdNotNullAndActivatedIsTrue(pageable).map { UserDTO(it) }
    }

    @Transactional(readOnly = true)
    fun countManagedUsers() = userRepository.count()

    @Transactional(readOnly = true)
    fun getUserWithAuthoritiesByLogin(login: String): Mono<User> =
        userRepository.findOneWithAuthoritiesByLogin(login)

    /**
     * @return a list of all the authorities
     */
    @Transactional(readOnly = true)
    fun getAuthorities() =
        authorityRepository.findAll().map(Authority::name)

    private fun syncUserWithIdP(details: Map<String, Any>, user: User): Mono<User> {
        // save authorities in to sync user roles/groups between IdP and JHipster's local database
        val userAuthorities =
            user.authorities.map { it.name }.toList()

        return getAuthorities().collectList()
            .flatMapMany { dbAuthorities: List<String?> ->
                val authoritiesToSave = userAuthorities.filter { authority: String? -> !dbAuthorities.contains(authority) }
                    .map { authority: String? ->
                        val authorityToSave = Authority()
                        authorityToSave.name = authority
                        authorityToSave
                    }
                Flux.fromIterable(authoritiesToSave)
            }
            .doOnNext { authority: Authority? -> log.debug("Saving authority '$authority' in local database") }
            .flatMap<Authority> { authorityRepository.save(it) }
            .then(userRepository.findOneByLogin(user.login!!))
            .switchIfEmpty(saveUser(user, true))
            .flatMap<Any> { existingUser: User ->
                // if IdP sends last updated information, use it to determine if an update should happen
                if (details["updated_at"] != null) {
                    val dbModifiedDate = existingUser.lastModifiedDate
                    val idpModifiedDate = details["updated_at"] as Instant
                    if (idpModifiedDate.isAfter(dbModifiedDate)) {
                        log.debug("Updating user '${user.login}' in local database")
                        return@flatMap updateUser(user.firstName, user.lastName, user.email, user.langKey, user.imageUrl)
                    }
                    // no last updated info, blindly update
                } else {
                    log.debug("Updating user '${user.login}' in local database")
                    return@flatMap updateUser(user.firstName, user.lastName, user.email, user.langKey, user.imageUrl)
                }
                return@flatMap Mono.empty<User>()
            }
            .thenReturn(user)
    }

    /**
     * Returns the user from an OAuth 2.0 login or resource server with JWT.
     * Synchronizes the user in the local repository.
     *
     * @param authToken the authentication token.
     * @return the user from the authentication.
     */
    @Transactional
    fun getUserFromAuthentication(authToken: AbstractAuthenticationToken): Mono<AdminUserDTO> {
        val attributes: Map<String, Any> =
            when (authToken) {
                is OAuth2AuthenticationToken -> authToken.principal.attributes
                is JwtAuthenticationToken -> authToken.tokenAttributes
                else -> throw IllegalArgumentException("AuthenticationToken is not OAuth2 or JWT!")
            }

        val user = getUser(attributes)
        user.authorities = authToken.authorities.asSequence()
            .map(GrantedAuthority::getAuthority)
            .map { Authority(name = it) }
            .toMutableSet()
        return syncUserWithIdP(attributes, user).flatMap { Mono.just(AdminUserDTO(it)) }
    }

    companion object {

        @JvmStatic
        private fun getUser(details: Map<String, Any>): User {
            var activated = true
            val user = User()
            // handle resource server JWT, where sub claim is email and uid is ID
            if (details["uid"] != null) {
                user.id = details["uid"] as String
                user.login = details["sub"] as String
            } else {
                user.id = details["sub"] as String
            }
            if (details["preferred_username"] != null) {
                user.login = (details["preferred_username"] as String).toLowerCase()
            } else if (user.login == null) {
                user.login = user.id
            }
            if (details["given_name"] != null) {
                user.firstName = details["given_name"] as String
            }
            if (details["family_name"] != null) {
                user.lastName = details["family_name"] as String
            }
            if (details["email_verified"] != null) {
                activated = details["email_verified"] as Boolean
            }
            if (details["email"] != null) {
                user.email = (details["email"] as String).toLowerCase()
            } else {
                user.email = details["sub"] as String
            }
            if (details["langKey"] != null) {
                user.langKey = details["langKey"] as String
            } else if (details["locale"] != null) {
                // trim off country code if it exists
                var locale = details["locale"] as String
                if (locale.contains("_")) {
                    locale = locale.substring(0, locale.indexOf("_"))
                } else if (locale.contains("-")) {
                    locale = locale.substring(0, locale.indexOf("-"))
                }
                user.langKey = locale.toLowerCase()
            } else {
                // set langKey to default if not specified by IdP
                user.langKey = DEFAULT_LANGUAGE
            }
            if (details["picture"] != null) {
                user.imageUrl = details["picture"] as String
            }
            user.activated = activated
            return user
        }
    }
}
