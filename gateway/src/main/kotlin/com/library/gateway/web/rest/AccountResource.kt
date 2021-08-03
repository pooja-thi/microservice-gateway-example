package com.library.gateway.web.rest

import com.library.gateway.service.UserService
import com.library.gateway.service.dto.AdminUserDTO
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.security.Principal

/**
 * REST controller for managing the current user's account.
 */
@RestController
@RequestMapping("/api")
class AccountResource(private val userService: UserService) {

    internal class AccountResourceException(message: String) : RuntimeException(message)

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * `GET  /account` : get the current user.
     *
     * @param principal the current user; resolves to `null` if not authenticated.
     * @return the current user.
     * @throws AccountResourceException `500 (Internal Server Error)` if the user couldn't be returned.
     */
    @GetMapping("/account")
    fun getAccount(principal: Principal?): Mono<AdminUserDTO> =
        if (principal is AbstractAuthenticationToken) {
            userService.getUserFromAuthentication(principal)
        } else {
            throw AccountResourceException("User could not be found")
        }

    companion object {
        private const val serialVersionUID = 1L
    }
}
