package com.library.user.repository

import com.library.user.domain.Address
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

/**
 * Spring Data SQL repository for the [Address] entity.
 */
@Suppress("unused")
@Repository
interface AddressRepository : JpaRepository<Address, Long>
