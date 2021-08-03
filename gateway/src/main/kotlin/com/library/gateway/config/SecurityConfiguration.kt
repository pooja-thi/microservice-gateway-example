package com.library.gateway.config

import com.library.gateway.security.ADMIN
import com.library.gateway.security.extractAuthorityFromClaims
import com.library.gateway.security.oauth2.AudienceValidator
import com.library.gateway.security.oauth2.JwtGrantedAuthorityConverter
import com.library.gateway.web.filter.SpaWebFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.core.convert.converter.Converter
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.oauth2.client.oidc.userinfo.OidcReactiveOAuth2UserService
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUser
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority
import org.springframework.security.oauth2.jwt.*
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository
import org.springframework.security.web.server.header.ReferrerPolicyServerHttpHeadersWriter
import org.springframework.security.web.server.util.matcher.NegatedServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.OrServerWebExchangeMatcher
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers.pathMatchers
import org.zalando.problem.spring.webflux.advice.security.SecurityProblemSupport
import reactor.core.publisher.Mono
import tech.jhipster.config.JHipsterProperties
import tech.jhipster.web.filter.reactive.CookieCsrfFilter

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Import(SecurityProblemSupport::class)
class SecurityConfiguration(
    private val jHipsterProperties: JHipsterProperties,
    private val problemSupport: SecurityProblemSupport
) {

    @Value("\${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private lateinit var issuerUri: String

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        // @formatter:off
        http
            .securityMatcher(
                NegatedServerWebExchangeMatcher(
                    OrServerWebExchangeMatcher(
                        pathMatchers("/app/**", "/i18n/**", "/content/**", "/swagger-ui/**", "/swagger-resources/**", "/v2/api-docs", "/v3/api-docs", "/test/**"),
                        pathMatchers(HttpMethod.OPTIONS, "/**")
                    )
                )
            )
            .csrf()
            .csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse())
            .and()
            // See https://github.com/spring-projects/spring-security/issues/5766
            .addFilterAt(CookieCsrfFilter(), SecurityWebFiltersOrder.REACTOR_CONTEXT)
            .addFilterAt(SpaWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            .exceptionHandling()
            .accessDeniedHandler(problemSupport)
            .authenticationEntryPoint(problemSupport)
            .and()
            .headers()
            .contentSecurityPolicy(jHipsterProperties.security.contentSecurityPolicy)
            .and()
            .referrerPolicy(ReferrerPolicyServerHttpHeadersWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)
            .and()
            .featurePolicy("geolocation 'none'; midi 'none'; sync-xhr 'none'; microphone 'none'; camera 'none'; magnetometer 'none'; gyroscope 'none'; speaker 'none'; fullscreen 'self'; payment 'none'")
            .and()
            .frameOptions().disable()
            .and()
            .authorizeExchange()
            .pathMatchers("/").permitAll()
            .pathMatchers("/*.*").permitAll()
            .pathMatchers("/api/auth-info").permitAll()
            .pathMatchers("/api/**").authenticated()
            .pathMatchers("/services/**").authenticated()
            .pathMatchers("/management/health").permitAll()
            .pathMatchers("/management/info").permitAll()
            .pathMatchers("/management/prometheus").permitAll()
            .pathMatchers("/management/**").hasAuthority(ADMIN)

        http.oauth2Login()
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(jwtAuthenticationConverter())

        http.oauth2Client()
        // @formatter:on
        return http.build()
    }

    fun jwtAuthenticationConverter(): Converter<Jwt, Mono<AbstractAuthenticationToken>> {
        val jwtAuthenticationConverter = JwtAuthenticationConverter()
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(JwtGrantedAuthorityConverter())
        return ReactiveJwtAuthenticationConverterAdapter(jwtAuthenticationConverter)
    }

    /**
     * Map authorities from "groups" or "roles" claim in ID Token.
     *
     * @return a {@link ReactiveOAuth2UserService} that has the groups from the IdP.
     */
    @Bean
    fun oidcUserService(): ReactiveOAuth2UserService<OidcUserRequest, OidcUser> {
        val delegate = OidcReactiveOAuth2UserService()

        // Delegate to the default implementation for loading a user
        return ReactiveOAuth2UserService { userRequest ->
            delegate.loadUser(userRequest).map { oidcUser ->
                val mappedAuthorities = hashSetOf<GrantedAuthority>()
                oidcUser.authorities.forEach { grantedAuthority ->
                    if (grantedAuthority is OidcUserAuthority) {
                        extractAuthorityFromClaims(grantedAuthority.userInfo.claims)?.let { userInfoClaim -> mappedAuthorities.addAll(userInfoClaim) }
                    }
                }
                DefaultOidcUser(mappedAuthorities, oidcUser.idToken, oidcUser.userInfo)
            }
        }
    }

    @Bean
    fun jwtDecoder(): ReactiveJwtDecoder {
        val jwtDecoder = ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri) as NimbusReactiveJwtDecoder

        val audienceValidator = AudienceValidator(jHipsterProperties.security.oauth2.audience)
        val withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri)
        val withAudience = DelegatingOAuth2TokenValidator<Jwt>(withIssuer, audienceValidator)

        jwtDecoder.setJwtValidator(withAudience)

        return jwtDecoder
    }
}
