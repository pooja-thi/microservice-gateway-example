package com.library.user

import com.library.user.config.TestSecurityConfiguration
import org.springframework.boot.test.context.SpringBootTest

/**
 * Base composite annotation for integration tests.
 */
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [UserApp::class, TestSecurityConfiguration::class])
annotation class IntegrationTest
