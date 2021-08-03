
package com.library.gateway.web.rest

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository
import org.springframework.security.oauth2.core.oidc.OidcIdToken
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono

/**
 * REST controller for managing global OIDC logout.
 */
@RestController
class LogoutResource(registrations: ReactiveClientRegistrationRepository) {
    private val registration = registrations.findByRegistrationId("oidc")

    /**
     * `POST  /api/logout` : logout the current user.
     *
     * @param idToken the ID token.
     * @param session the current {@link WebSession}.
     * @return the [ResponseEntity] with status `200 (OK)` and a body with a global logout URL and ID token.
     */
    @PostMapping("/api/logout")
    fun logout(@AuthenticationPrincipal(expression = "idToken") idToken: OidcIdToken, session: WebSession): Mono<HashMap<String, String>> {
        return session.invalidate().then(
            registration.map { it.providerDetails.configurationMetadata["end_session_endpoint"].toString() }
                .map {
                    val logoutDetails = hashMapOf<String, String>()
                    logoutDetails["logoutUrl"] = it
                    logoutDetails["idToken"] = idToken.tokenValue
                    logoutDetails
                }
        )
    }
}
