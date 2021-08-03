package com.library.gateway.web.rest.errors

import com.library.gateway.IntegrationTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.csrf
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Integration tests [ExceptionTranslator] controller advice.
 */
@WithMockUser
@AutoConfigureWebTestClient
@IntegrationTest
class ExceptionTranslatorIT {

    @Autowired
    private lateinit var webTestClient: WebTestClient

    @BeforeEach
    fun setupCsrf() {
        webTestClient = webTestClient.mutateWith(csrf())
    }

    @Test
    fun testConcurrencyFailure() {
        webTestClient.get().uri("/api/exception-translator-test/concurrency-failure")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.CONFLICT)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("\$.message").isEqualTo(ERR_CONCURRENCY_FAILURE)
    }

    @Test
    fun testMethodArgumentNotValid() {
        webTestClient.post().uri("/api/exception-translator-test/method-argument")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{}")
            .exchange()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("\$.message").isEqualTo(ERR_VALIDATION)
            .jsonPath("\$.fieldErrors.[0].objectName").isEqualTo("test")
            .jsonPath("\$.fieldErrors.[0].field").isEqualTo("test")
            .jsonPath("\$.fieldErrors.[0].message").isEqualTo("must not be null")
    }

    @Test
    fun testMissingRequestPart() {
        webTestClient.get().uri("/api/exception-translator-test/missing-servlet-request-part")
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("\$.message").isEqualTo("error.http.400")
    }

    @Test
    fun testMissingRequestParameter() {
        webTestClient.get().uri("/api/exception-translator-test/missing-servlet-request-parameter")
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("\$.message").isEqualTo("error.http.400")
    }

    @Test
    fun testAccessDenied() {
        webTestClient.get().uri("/api/exception-translator-test/access-denied")
            .exchange()
            .expectStatus().isForbidden
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("\$.message").isEqualTo("error.http.403")
            .jsonPath("\$.detail").isEqualTo("test access denied!")
    }

    @Test
    fun testUnauthorized() {
        webTestClient.get().uri("/api/exception-translator-test/unauthorized")
            .exchange()
            .expectStatus().isUnauthorized
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("\$.message").isEqualTo("error.http.401")
            .jsonPath("$.path").isEqualTo("/api/exception-translator-test/unauthorized")
            .jsonPath("\$.detail").isEqualTo("test authentication failed!")
    }

    @Test
    fun testMethodNotSupported() {
        webTestClient.post().uri("/api/exception-translator-test/access-denied")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.METHOD_NOT_ALLOWED)
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("\$.message").isEqualTo("error.http.405")
            .jsonPath("\$.detail").isEqualTo("405 METHOD_NOT_ALLOWED \"Request method 'POST' not supported\"")
    }

    @Test
    fun testExceptionWithResponseStatus() {
        webTestClient.get().uri("/api/exception-translator-test/response-status")
            .exchange()
            .expectStatus().isBadRequest
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("\$.message").isEqualTo("error.http.400")
            .jsonPath("\$.title").isEqualTo("test response status")
    }

    @Test
    fun testInternalServerError() {
        webTestClient.get().uri("/api/exception-translator-test/internal-server-error")
            .exchange()
            .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
            .expectBody()
            .jsonPath("\$.message").isEqualTo("error.http.500")
            .jsonPath("\$.title").isEqualTo("Internal Server Error")
    }
}
