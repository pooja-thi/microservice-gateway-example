package com.library.user.repository

import com.library.user.domain.Customer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data SQL repository for the [Customer] entity.
 */
@Suppress("unused")
@Repository
interface CustomerRepository : JpaRepository<Customer, Long>
