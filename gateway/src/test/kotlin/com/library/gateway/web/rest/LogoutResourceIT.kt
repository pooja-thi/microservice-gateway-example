package com.library.gateway.web.rest

import com.library.gateway.IntegrationTest
import com.library.gateway.security.USER
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant

/**
 * Integration tests for the [LogoutResource] REST controller.
 */
@IntegrationTest
class LogoutResourceIT {

    @Autowired
    private lateinit var registrations: ReactiveClientRegistrationRepository

    @Autowired
    private lateinit var context: ApplicationContext

    private lateinit var webTestClient: WebTestClient

    private lateinit var idToken: OidcIdToken

    @BeforeEach

    fun before() {
        val claims = mapOf(
            "groups" to listOf(USER),
            "sub" to 123
        )
        val idToken = OidcIdToken(ID_TOKEN, Instant.now(), Instant.now().plusSeconds(60), claims)
        val springSecurity = springSecurity()
        val webTestClient: WebTestClient.MockServerSpec<*> = WebTestClient
            .bindToApplicationContext(context!!)
            .apply(springSecurity!!)
    }
}
