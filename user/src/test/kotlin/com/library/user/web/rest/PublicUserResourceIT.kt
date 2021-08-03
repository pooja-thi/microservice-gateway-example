package com.library.user.web.rest

import com.library.user.IntegrationTest
import com.library.user.domain.User
import com.library.user.repository.UserRepository
import com.library.user.security.ADMIN
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.cache.CacheManager
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityManager

/**
 * Integration tests for the {@link UserResource} REST controller.
 */
@AutoConfigureMockMvc
@WithMockUser(authorities = [ADMIN])
@IntegrationTest
class PublicUserResourceIT {

    private val DEFAULT_LOGIN = "johndoe"

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var cacheManager: CacheManager

    @Autowired
    private lateinit var restUserMockMvc: MockMvc

    private lateinit var user: User

    @BeforeEach
    fun setup() {
        cacheManager.getCache(UserRepository.USERS_BY_LOGIN_CACHE).clear()
        cacheManager.getCache(UserRepository.USERS_BY_EMAIL_CACHE).clear()
    }

    @BeforeEach
    fun initTest() {
        user = UserResourceIT.initTestUser(userRepository, em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllPublicUsers() {
        // Initialize the database
        userRepository.saveAndFlush(user)

        // Get all the users
        restUserMockMvc.perform(
            get("/api/users?sort=id,desc")
                .accept(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].login").value(hasItem<String>(DEFAULT_LOGIN)))
            .andExpect(jsonPath("$.[*].email").doesNotExist())
            .andExpect(jsonPath("$.[*].imageUrl").doesNotExist())
            .andExpect(jsonPath("$.[*].langKey").doesNotExist())
    }
}
