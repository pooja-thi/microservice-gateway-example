package com.library.user.domain

import com.library.user.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AddressTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Address::class)
        val address1 = Address()
        address1.id = 1L
        val address2 = Address()
        address2.id = address1.id
        assertThat(address1).isEqualTo(address2)
        address2.id = 2L
        assertThat(address1).isNotEqualTo(address2)
        address1.id = null
        assertThat(address1).isNotEqualTo(address2)
    }
}
