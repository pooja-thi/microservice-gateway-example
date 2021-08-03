package com.library.user.domain

import com.library.user.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CustomerTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Customer::class)
        val customer1 = Customer()
        customer1.id = 1L
        val customer2 = Customer()
        customer2.id = customer1.id
        assertThat(customer1).isEqualTo(customer2)
        customer2.id = 2L
        assertThat(customer1).isNotEqualTo(customer2)
        customer1.id = null
        assertThat(customer1).isNotEqualTo(customer2)
    }
}
