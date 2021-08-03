package com.library.user.web.rest

import com.library.user.IntegrationTest
import com.library.user.domain.Address
import com.library.user.repository.AddressRepository
import com.library.user.web.rest.errors.ExceptionTranslator
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.web.PageableHandlerMethodArgumentResolver
import org.springframework.http.MediaType
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import org.springframework.validation.Validator
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [AddressResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class AddressResourceIT {
    @Autowired
    private lateinit var addressRepository: AddressRepository

    @Autowired
    private lateinit var jacksonMessageConverter: MappingJackson2HttpMessageConverter

    @Autowired
    private lateinit var pageableArgumentResolver: PageableHandlerMethodArgumentResolver

    @Autowired
    private lateinit var exceptionTranslator: ExceptionTranslator

    @Autowired
    private lateinit var validator: Validator

    @Autowired
    private lateinit var em: EntityManager

    @Autowired
    private lateinit var restAddressMockMvc: MockMvc

    private lateinit var address: Address

    @BeforeEach
    fun initTest() {
        address = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createAddress() {
        val databaseSizeBeforeCreate = addressRepository.findAll().size

        // Create the Address
        restAddressMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(address))
        ).andExpect(status().isCreated)

        // Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeCreate + 1)
        val testAddress = addressList[addressList.size - 1]

        assertThat(testAddress.address1).isEqualTo(DEFAULT_ADDRESS_1)
        assertThat(testAddress.address2).isEqualTo(DEFAULT_ADDRESS_2)
        assertThat(testAddress.city).isEqualTo(DEFAULT_CITY)
        assertThat(testAddress.postcode).isEqualTo(DEFAULT_POSTCODE)
        assertThat(testAddress.country).isEqualTo(DEFAULT_COUNTRY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createAddressWithExistingId() {
        // Create the Address with an existing ID
        address.id = 1L

        val databaseSizeBeforeCreate = addressRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restAddressMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(address))
        ).andExpect(status().isBadRequest)

        // Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkPostcodeIsRequired() {
        val databaseSizeBeforeTest = addressRepository.findAll().size
        // set the field null
        address.postcode = null

        // Create the Address, which fails.

        restAddressMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(address))
        ).andExpect(status().isBadRequest)

        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkCountryIsRequired() {
        val databaseSizeBeforeTest = addressRepository.findAll().size
        // set the field null
        address.country = null

        // Create the Address, which fails.

        restAddressMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(address))
        ).andExpect(status().isBadRequest)

        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllAddresses() {
        // Initialize the database
        addressRepository.saveAndFlush(address)

        // Get all the addressList
        restAddressMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(address.id?.toInt())))
            .andExpect(jsonPath("$.[*].address1").value(hasItem(DEFAULT_ADDRESS_1)))
            .andExpect(jsonPath("$.[*].address2").value(hasItem(DEFAULT_ADDRESS_2)))
            .andExpect(jsonPath("$.[*].city").value(hasItem(DEFAULT_CITY)))
            .andExpect(jsonPath("$.[*].postcode").value(hasItem(DEFAULT_POSTCODE)))
            .andExpect(jsonPath("$.[*].country").value(hasItem(DEFAULT_COUNTRY)))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAddress() {
        // Initialize the database
        addressRepository.saveAndFlush(address)

        val id = address.id
        assertNotNull(id)

        // Get the address
        restAddressMockMvc.perform(get(ENTITY_API_URL_ID, address.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(address.id?.toInt()))
            .andExpect(jsonPath("$.address1").value(DEFAULT_ADDRESS_1))
            .andExpect(jsonPath("$.address2").value(DEFAULT_ADDRESS_2))
            .andExpect(jsonPath("$.city").value(DEFAULT_CITY))
            .andExpect(jsonPath("$.postcode").value(DEFAULT_POSTCODE))
            .andExpect(jsonPath("$.country").value(DEFAULT_COUNTRY))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingAddress() {
        // Get the address
        restAddressMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewAddress() {
        // Initialize the database
        addressRepository.saveAndFlush(address)

        val databaseSizeBeforeUpdate = addressRepository.findAll().size

        // Update the address
        val updatedAddress = addressRepository.findById(address.id).get()
        // Disconnect from session so that the updates on updatedAddress are not directly saved in db
        em.detach(updatedAddress)
        updatedAddress.address1 = UPDATED_ADDRESS_1
        updatedAddress.address2 = UPDATED_ADDRESS_2
        updatedAddress.city = UPDATED_CITY
        updatedAddress.postcode = UPDATED_POSTCODE
        updatedAddress.country = UPDATED_COUNTRY

        restAddressMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedAddress.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedAddress))
        ).andExpect(status().isOk)

        // Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeUpdate)
        val testAddress = addressList[addressList.size - 1]
        assertThat(testAddress.address1).isEqualTo(UPDATED_ADDRESS_1)
        assertThat(testAddress.address2).isEqualTo(UPDATED_ADDRESS_2)
        assertThat(testAddress.city).isEqualTo(UPDATED_CITY)
        assertThat(testAddress.postcode).isEqualTo(UPDATED_POSTCODE)
        assertThat(testAddress.country).isEqualTo(UPDATED_COUNTRY)
    }

    @Test
    @Transactional
    fun putNonExistingAddress() {
        val databaseSizeBeforeUpdate = addressRepository.findAll().size
        address.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAddressMockMvc.perform(
            put(ENTITY_API_URL_ID, address.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(address))
        )
            .andExpect(status().isBadRequest)

        // Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchAddress() {
        val databaseSizeBeforeUpdate = addressRepository.findAll().size
        address.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAddressMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(address))
        ).andExpect(status().isBadRequest)

        // Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamAddress() {
        val databaseSizeBeforeUpdate = addressRepository.findAll().size
        address.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAddressMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(address))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateAddressWithPatch() {

        // Initialize the database
        addressRepository.saveAndFlush(address)

        val databaseSizeBeforeUpdate = addressRepository.findAll().size

// Update the address using partial update
        val partialUpdatedAddress = Address().apply {
            id = address.id

            address1 = UPDATED_ADDRESS_1
            postcode = UPDATED_POSTCODE
        }

        restAddressMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedAddress.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedAddress))
        )
            .andExpect(status().isOk)

// Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeUpdate)
        val testAddress = addressList.last()
        assertThat(testAddress.address1).isEqualTo(UPDATED_ADDRESS_1)
        assertThat(testAddress.address2).isEqualTo(DEFAULT_ADDRESS_2)
        assertThat(testAddress.city).isEqualTo(DEFAULT_CITY)
        assertThat(testAddress.postcode).isEqualTo(UPDATED_POSTCODE)
        assertThat(testAddress.country).isEqualTo(DEFAULT_COUNTRY)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateAddressWithPatch() {

        // Initialize the database
        addressRepository.saveAndFlush(address)

        val databaseSizeBeforeUpdate = addressRepository.findAll().size

// Update the address using partial update
        val partialUpdatedAddress = Address().apply {
            id = address.id

            address1 = UPDATED_ADDRESS_1
            address2 = UPDATED_ADDRESS_2
            city = UPDATED_CITY
            postcode = UPDATED_POSTCODE
            country = UPDATED_COUNTRY
        }

        restAddressMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedAddress.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedAddress))
        )
            .andExpect(status().isOk)

// Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeUpdate)
        val testAddress = addressList.last()
        assertThat(testAddress.address1).isEqualTo(UPDATED_ADDRESS_1)
        assertThat(testAddress.address2).isEqualTo(UPDATED_ADDRESS_2)
        assertThat(testAddress.city).isEqualTo(UPDATED_CITY)
        assertThat(testAddress.postcode).isEqualTo(UPDATED_POSTCODE)
        assertThat(testAddress.country).isEqualTo(UPDATED_COUNTRY)
    }

    @Throws(Exception::class)
    fun patchNonExistingAddress() {
        val databaseSizeBeforeUpdate = addressRepository.findAll().size
        address.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restAddressMockMvc.perform(
            patch(ENTITY_API_URL_ID, address.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(address))
        )
            .andExpect(status().isBadRequest)

        // Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchAddress() {
        val databaseSizeBeforeUpdate = addressRepository.findAll().size
        address.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAddressMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(address))
        )
            .andExpect(status().isBadRequest)

        // Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamAddress() {
        val databaseSizeBeforeUpdate = addressRepository.findAll().size
        address.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restAddressMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(address))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Address in the database
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteAddress() {
        // Initialize the database
        addressRepository.saveAndFlush(address)

        val databaseSizeBeforeDelete = addressRepository.findAll().size

        // Delete the address
        restAddressMockMvc.perform(
            delete(ENTITY_API_URL_ID, address.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val addressList = addressRepository.findAll()
        assertThat(addressList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_ADDRESS_1 = "AAAAAAAAAA"
        private const val UPDATED_ADDRESS_1 = "BBBBBBBBBB"

        private const val DEFAULT_ADDRESS_2 = "AAAAAAAAAA"
        private const val UPDATED_ADDRESS_2 = "BBBBBBBBBB"

        private const val DEFAULT_CITY = "AAAAAAAAAA"
        private const val UPDATED_CITY = "BBBBBBBBBB"

        private const val DEFAULT_POSTCODE = "AAAAAAAAAA"
        private const val UPDATED_POSTCODE = "BBBBBBBBBB"

        private const val DEFAULT_COUNTRY = "AA"
        private const val UPDATED_COUNTRY = "BB"

        private val ENTITY_API_URL: String = "/api/addresses"
        private val ENTITY_API_URL_ID: String = ENTITY_API_URL + "/{id}"

        private val random: Random = Random()
        private val count: AtomicLong = AtomicLong(random.nextInt().toLong() + (2 * Integer.MAX_VALUE))

        /**
         * Create an entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createEntity(em: EntityManager): Address {
            val address = Address(

                address1 = DEFAULT_ADDRESS_1,

                address2 = DEFAULT_ADDRESS_2,

                city = DEFAULT_CITY,

                postcode = DEFAULT_POSTCODE,

                country = DEFAULT_COUNTRY

            )

            return address
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Address {
            val address = Address(

                address1 = UPDATED_ADDRESS_1,

                address2 = UPDATED_ADDRESS_2,

                city = UPDATED_CITY,

                postcode = UPDATED_POSTCODE,

                country = UPDATED_COUNTRY

            )

            return address
        }
    }
}
