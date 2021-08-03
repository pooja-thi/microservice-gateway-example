package com.library.publication.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Properties specific to Publication.
 *
 * Properties are configured in the `application.yml` file.
 * See [tech.jhipster.config.JHipsterProperties] for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
class ApplicationProperties
