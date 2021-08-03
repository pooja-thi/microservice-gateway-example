package com.library.publication.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor
import tech.jhipster.config.locale.AngularCookieLocaleResolver

@Configuration
class LocaleConfiguration : WebMvcConfigurer {

    @Bean
    fun localeResolver() = AngularCookieLocaleResolver().apply { cookieName = "NG_TRANSLATE_LANG_KEY" }

    override fun addInterceptors(registry: InterceptorRegistry?) {
        registry!!.addInterceptor(LocaleChangeInterceptor().apply { paramName = "language" })
    }
}
