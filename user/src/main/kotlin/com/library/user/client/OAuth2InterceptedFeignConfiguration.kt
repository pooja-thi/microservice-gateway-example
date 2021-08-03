package com.library.user.client

import com.library.user.security.oauth2.AuthorizationHeaderUtil
import org.springframework.context.annotation.Bean

class OAuth2InterceptedFeignConfiguration {

    @Bean(name = ["oauth2RequestInterceptor"])
    fun getOAuth2RequestInterceptor(authorizationHeaderUtil: AuthorizationHeaderUtil) =
        TokenRelayRequestInterceptor(authorizationHeaderUtil)
}
