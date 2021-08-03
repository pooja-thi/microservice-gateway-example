package com.library.publication

import com.library.publication.config.TestSecurityConfiguration
import org.springframework.boot.test.context.SpringBootTest

/**
 * Base composite annotation for integration tests.
 */
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [PublicationApp::class, TestSecurityConfiguration::class])
annotation class IntegrationTest
