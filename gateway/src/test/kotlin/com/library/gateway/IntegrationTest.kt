package com.library.gateway

import com.library.gateway.config.TestSecurityConfiguration
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest

/**
 * Base composite annotation for integration tests.
 */
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [GatewayApp::class, TestSecurityConfiguration::class])
@ExtendWith(ReactiveSqlTestContainerExtension::class)
annotation class IntegrationTest
