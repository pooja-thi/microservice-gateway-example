package com.library.publication.repository

import com.library.publication.domain.Book
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data SQL repository for the [Book] entity.
 */
@Suppress("unused")
@Repository
interface BookRepository : JpaRepository<Book, Long>
