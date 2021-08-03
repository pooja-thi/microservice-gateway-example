package com.library.gateway.web.rest

import com.library.gateway.security.ADMIN
import com.library.gateway.service.UserService
import com.library.gateway.service.dto.AdminUserDTO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import tech.jhipster.web.util.PaginationUtil

/**
 * REST controller for managing users.
 *
 * This class accesses the [com.library.gateway.domain.User] entity, and needs to fetch its collection of authorities.
 *
 * For a normal use-case, it would be better to have an eager relationship between User and Authority,
 * and send everything to the client side: there would be no View Model and DTO, a lot less code, and an outer-join
 * which would be good for performance.
 *
 * We use a View Model and a DTO for 3 reasons:
 *
 * * We want to keep a lazy association between the user and the authorities, because people will
 * quite often do relationships with the user, and we don't want them to get the authorities all
 * the time for nothing (for performance reasons). This is the #1 goal: we should not impact our users'
 * application because of this use-case.
 * *  Not having an outer join causes n+1 requests to the database. This is not a real issue as
 * we have by default a second-level cache. This means on the first HTTP call we do the n+1 requests,
 * but then all authorities come from the cache, so in fact it's much better than doing an outer join
 * (which will get lots of data from the database, for each HTTP call).
 * *  As this manages users, for security reasons, we'd rather have a DTO layer.
 *
 * Another option would be to have a specific JPA entity graph to handle this case.
 */
@RestController
@RequestMapping("/api/admin")
class UserResource(
    private val userService: UserService
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${jhipster.clientApp.name}")
    private val applicationName: String? = null

    /**
     * `GET /admin/users` : get all users with all the details - calling this are only allowed for the administrators.
     *
     * @param request a [ServerHttpRequest] request.
     * @param pageable the pagination information.
     * @return the `ResponseEntity` with status `200 (OK)` and with body all users.
     */
    @GetMapping("/users")
    @PreAuthorize("hasAuthority(\"$ADMIN\")")
    fun getAllUsers(
        request: ServerHttpRequest,
        pageable: Pageable
    ): Mono<ResponseEntity<Flux<AdminUserDTO>>> {
        log.debug("REST request to get all User for an admin")

        return userService.countManagedUsers()
            .map { total -> PageImpl(mutableListOf<AdminUserDTO>(), pageable, total!!) }
            .map { page -> PaginationUtil.generatePaginationHttpHeaders(UriComponentsBuilder.fromHttpRequest(request), page) }
            .map { headers -> ResponseEntity.ok().headers(headers).body(userService.getAllManagedUsers(pageable)) }
    }

    /**
     * `GET /admin/users/:login` : get the "login" user.
     *
     * @param login the login of the user to find.
     * @return the `ResponseEntity` with status `200 (OK)` and with body the "login" user, or with status `404 (Not Found)`.
     */
    @GetMapping("/users/{login}")
    @PreAuthorize("hasAuthority(\"$ADMIN\")")
    fun getUser(@PathVariable login: String): Mono<AdminUserDTO> {
        log.debug("REST request to get User : $login")
        return userService.getUserWithAuthoritiesByLogin(login)
            .map { AdminUserDTO(it) }
            .switchIfEmpty(Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND)))
    }
}
