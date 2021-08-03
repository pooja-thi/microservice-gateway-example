package com.library.publication.client

import com.library.publication.security.oauth2.AuthorizationHeaderUtil
import org.springframework.context.annotation.Bean

class OAuth2InterceptedFeignConfiguration {

    @Bean(name = ["oauth2RequestInterceptor"])
    fun getOAuth2RequestInterceptor(authorizationHeaderUtil: AuthorizationHeaderUtil) =
        TokenRelayRequestInterceptor(authorizationHeaderUtil)
}
