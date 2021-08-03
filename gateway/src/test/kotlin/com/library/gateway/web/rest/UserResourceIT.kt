package com.library.gateway.web.rest

import com.library.gateway.IntegrationTest
import com.library.gateway.config.SYSTEM_ACCOUNT
import com.library.gateway.domain.Authority
import com.library.gateway.domain.User
import com.library.gateway.repository.AuthorityRepository
import com.library.gateway.repository.UserRepository
import com.library.gateway.security.ADMIN
import com.library.gateway.security.USER
import com.library.gateway.service.EntityManager
import com.library.gateway.service.dto.AdminUserDTO
import com.library.gateway.service.mapper.UserMapper
import org.apache.commons.lang3.RandomStringUtils
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant
import java.util.UUID
import kotlin.test.assertNotNull

/**
 * Integration tests for the [UserResource] REST controller.
 */
@AutoConfigureWebTestClient
@WithMockUser(authorities = [ADMIN])
@IntegrationTest
class UserResourceIT {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var authorityRepository: AuthorityRepository

    @Autowired
    private lateinit var userMapper: UserMapper

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var webTestClient: WebTestClient

    private lateinit var user: User

    @BeforeEach
    fun setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf())
    }

    @BeforeEach
    fun initTest() {
        user = initTestUser(userRepository, em)
    }

    @Test
    fun getAllUsers() {
        // Initialize the database
        userRepository.create(user).block()
        authorityRepository
            .findById(USER)
            .flatMap { authority -> authority.name?.let { user.id?.let { it1 -> userRepository.saveUserAuthority(it1, it) } } }
            .block()
        // Get all the users
        val foundUser = webTestClient.get().uri("/api/admin/users?sort=id,DESC")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .returnResult(AdminUserDTO::class.java).responseBody.blockFirst()

        assertNotNull(foundUser)
        assertThat(foundUser.login).isEqualTo(DEFAULT_LOGIN)
        assertThat(foundUser.firstName).isEqualTo(DEFAULT_FIRSTNAME)
        assertThat(foundUser.lastName).isEqualTo(DEFAULT_LASTNAME)
        assertThat(foundUser.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(foundUser.imageUrl).isEqualTo(DEFAULT_IMAGEURL)
        assertThat(foundUser.langKey).isEqualTo(DEFAULT_LANGKEY)
        assertThat(foundUser.authorities).containsExactly(USER)
    }

    @Test
    fun getUser() {
        // Initialize the database
        userRepository.create(user).block()
        authorityRepository
            .findById(USER)
            .flatMap { authority -> authority.name?.let { user.id?.let { it1 -> userRepository.saveUserAuthority(it1, it) } } }
            .block()

        // Get the user
        webTestClient.get().uri("/api/admin/users/{login}", user.login)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("\$.login").isEqualTo(user.login)
            .jsonPath("\$.firstName").isEqualTo(DEFAULT_FIRSTNAME)
            .jsonPath("\$.lastName").isEqualTo(DEFAULT_LASTNAME)
            .jsonPath("\$.email").isEqualTo(DEFAULT_EMAIL)
            .jsonPath("\$.imageUrl").isEqualTo(DEFAULT_IMAGEURL)
            .jsonPath("\$.langKey").isEqualTo(DEFAULT_LANGKEY)
            .jsonPath("\$.authorities").isEqualTo(USER)
    }

    @Test
    fun getNonExistingUser() {
        webTestClient.get().uri("/api/admin/users/unknown")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    @Throws(Exception::class)
    fun testUserEquals() {
        equalsVerifier(User::class)
        val user1 = User(id = DEFAULT_ID)
        val user2 = User(id = user1.id)
        assertThat(user1).isEqualTo(user2)
        user2.id = "id2"
        assertThat(user1).isNotEqualTo(user2)
        user1.id = null
        assertThat(user1).isNotEqualTo(user2)
    }

    @Test
    fun testUserDTOtoUser() {
        val userDTO = AdminUserDTO(
            id = DEFAULT_ID,
            login = DEFAULT_LOGIN,
            firstName = DEFAULT_FIRSTNAME,
            lastName = DEFAULT_LASTNAME,
            email = DEFAULT_EMAIL,
            activated = true,
            imageUrl = DEFAULT_IMAGEURL,
            langKey = DEFAULT_LANGKEY,
            createdBy = DEFAULT_LOGIN,
            lastModifiedBy = DEFAULT_LOGIN,
            authorities = mutableSetOf(USER)
        )

        val user = userMapper.userDTOToUser(userDTO)
        assertNotNull(user)
        assertThat(user.id).isEqualTo(DEFAULT_ID)
        assertThat(user.login).isEqualTo(DEFAULT_LOGIN)
        assertThat(user.firstName).isEqualTo(DEFAULT_FIRSTNAME)
        assertThat(user.lastName).isEqualTo(DEFAULT_LASTNAME)
        assertThat(user.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(user.activated).isTrue()
        assertThat(user.imageUrl).isEqualTo(DEFAULT_IMAGEURL)
        assertThat(user.langKey).isEqualTo(DEFAULT_LANGKEY)
        assertThat(user.createdBy).isNull()
        assertThat(user.createdDate).isNotNull()
        assertThat(user.lastModifiedBy).isNull()
        assertThat(user.lastModifiedDate).isNotNull()
        assertThat(user.authorities).extracting("name").containsExactly(USER)
    }

    @Test
    fun testUserToUserDTO() {
        user.id = DEFAULT_ID
        user.createdBy = DEFAULT_LOGIN
        user.createdDate = Instant.now()
        user.lastModifiedBy = DEFAULT_LOGIN
        user.lastModifiedDate = Instant.now()
        user.authorities = mutableSetOf(Authority(name = USER))

        val userDTO = userMapper.userToAdminUserDTO(user)

        assertThat(userDTO.id).isEqualTo(DEFAULT_ID)
        assertThat(userDTO.login).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.firstName).isEqualTo(DEFAULT_FIRSTNAME)
        assertThat(userDTO.lastName).isEqualTo(DEFAULT_LASTNAME)
        assertThat(userDTO.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(userDTO.activated).isTrue()
        assertThat(userDTO.imageUrl).isEqualTo(DEFAULT_IMAGEURL)
        assertThat(userDTO.langKey).isEqualTo(DEFAULT_LANGKEY)
        assertThat(userDTO.createdBy).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.createdDate).isEqualTo(user.createdDate)
        assertThat(userDTO.lastModifiedBy).isEqualTo(DEFAULT_LOGIN)
        assertThat(userDTO.lastModifiedDate).isEqualTo(user.lastModifiedDate)
        assertThat(userDTO.authorities).containsExactly(USER)
        assertThat(userDTO.toString()).isNotNull()
    }

    @Test
    fun testAuthorityEquals() {
        val authorityA = Authority()
        assertThat(authorityA)
            .isNotEqualTo(null)
            .isNotEqualTo(Any())
        assertThat(authorityA.hashCode()).isEqualTo(31)
        assertThat(authorityA.toString()).isNotNull()

        val authorityB = Authority()
        assertThat(authorityA.name).isEqualTo(authorityB.name)

        authorityB.name = ADMIN
        assertThat(authorityA).isNotEqualTo(authorityB)

        authorityA.name = USER
        assertThat(authorityA).isNotEqualTo(authorityB)

        authorityB.name = USER
        assertThat(authorityA)
            .isEqualTo(authorityB)
            .hasSameHashCodeAs(authorityB)
    }

    companion object {

        private const val DEFAULT_LOGIN = "johndoe"

        private const val DEFAULT_ID = "id1"

        private const val DEFAULT_EMAIL = "johndoe@localhost"

        private const val DEFAULT_FIRSTNAME = "john"

        private const val DEFAULT_LASTNAME = "doe"

        private const val DEFAULT_IMAGEURL = "http://placehold.it/50x50"

        private const val DEFAULT_LANGKEY = "en"

        /**
         * Create a User.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which has a required relationship to the User entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager?): User {
            return User(
                id = UUID.randomUUID().toString(),
                login = DEFAULT_LOGIN + RandomStringUtils.randomAlphabetic(5),
                activated = true,
                email = RandomStringUtils.randomAlphabetic(5) + DEFAULT_EMAIL,
                firstName = DEFAULT_FIRSTNAME,
                lastName = DEFAULT_LASTNAME,
                imageUrl = DEFAULT_IMAGEURL,
                createdBy = SYSTEM_ACCOUNT,
                langKey = DEFAULT_LANGKEY
            )
        }

        /**
         * Delete all the users from the database.
         */
        @JvmStatic
        fun deleteEntities(em: EntityManager) {
            try {
                em.deleteAll("jhi_user_authority").block()
                em.deleteAll(User::class.java).block()
            } catch (e: Exception) {
                // It can fail, if other entities are still referring this - it will be removed later.
            }
        }

        /**
         * Setups the database with one user.
         */
        @JvmStatic
        fun initTestUser(userRepository: UserRepository, em: EntityManager): User {
            userRepository.deleteAllUserAuthorities().block()
            userRepository.deleteAll().block()
            val user = createEntity(em)
            user.login = DEFAULT_LOGIN
            user.email = DEFAULT_EMAIL
            return user
        }
    }

    fun assertPersistedUsers(userAssertion: (List<User>) -> Unit) {
        userAssertion(userRepository.findAll().collectList().block())
    }
}
