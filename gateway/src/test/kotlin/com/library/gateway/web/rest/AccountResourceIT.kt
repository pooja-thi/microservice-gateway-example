package com.library.gateway.web.rest

import com.library.gateway.IntegrationTest
import com.library.gateway.security.ADMIN
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.MediaType
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.Instant

/**
 * Integration tests for the {@link AccountResource} REST controller.
 */
@AutoConfigureWebTestClient
@WithMockUser(value = TEST_USER_LOGIN)
@IntegrationTest
class AccountResourceIT {

    private lateinit var idToken: OidcIdToken

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setup() {
        val claims: MutableMap<String, Any> = hashMapOf()
        claims["groups"] = listOf(ADMIN)
        claims["sub"] = "jane"
        claims["email"] = "jane.doe@jhipster.com"
        this.idToken = OidcIdToken(
            ID_TOKEN,
            Instant.now(),
            Instant.now().plusSeconds(60),
            claims
        )
    }

    @Test

    fun testGetExistingAccount() {
        webTestClient
            .mutateWith(mockAuthentication(authenticationToken(idToken)))
            .mutateWith(csrf())
            .get().uri("/api/account")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.login").isEqualTo("jane")
            .jsonPath("$.email").isEqualTo("jane.doe@jhipster.com")
            .jsonPath("$.authorities").isEqualTo(ADMIN)
    }

    @Test

    fun testGetUnknownAccount() {
        webTestClient.get().uri("/api/account")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().is5xxServerError()
    }
}
