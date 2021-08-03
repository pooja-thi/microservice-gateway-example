package com.library.publication.domain

import com.library.publication.web.rest.equalsVerifier
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CategoryTest {

    @Test
    fun equalsVerifier() {
        equalsVerifier(Category::class)
        val category1 = Category()
        category1.id = 1L
        val category2 = Category()
        category2.id = category1.id
        assertThat(category1).isEqualTo(category2)
        category2.id = 2L
        assertThat(category1).isNotEqualTo(category2)
        category1.id = null
        assertThat(category1).isNotEqualTo(category2)
    }
}
