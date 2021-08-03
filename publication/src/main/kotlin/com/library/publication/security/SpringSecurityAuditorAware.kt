package com.library.publication.security

import com.library.publication.config.SYSTEM_ACCOUNT
import org.springframework.data.domain.AuditorAware
import org.springframework.stereotype.Component
import java.util.Optional

/**
 * Implementation of [AuditorAware] based on Spring Security.
 */
@Component
class SpringSecurityAuditorAware : AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> = Optional.of(getCurrentUserLogin().orElse(SYSTEM_ACCOUNT))
}
