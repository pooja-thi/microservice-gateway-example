package com.library.user.web.rest

import com.library.user.IntegrationTest
import com.library.user.domain.Customer
import com.library.user.repository.CustomerRepository
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
 * Integration tests for the [CustomerResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class CustomerResourceIT {
    @Autowired
    private lateinit var customerRepository: CustomerRepository

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
    private lateinit var restCustomerMockMvc: MockMvc

    private lateinit var customer: Customer

    @BeforeEach
    fun initTest() {
        customer = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCustomer() {
        val databaseSizeBeforeCreate = customerRepository.findAll().size

        // Create the Customer
        restCustomerMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(customer))
        ).andExpect(status().isCreated)

        // Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeCreate + 1)
        val testCustomer = customerList[customerList.size - 1]

        assertThat(testCustomer.firstName).isEqualTo(DEFAULT_FIRST_NAME)
        assertThat(testCustomer.lastName).isEqualTo(DEFAULT_LAST_NAME)
        assertThat(testCustomer.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(testCustomer.telephone).isEqualTo(DEFAULT_TELEPHONE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCustomerWithExistingId() {
        // Create the Customer with an existing ID
        customer.id = 1L

        val databaseSizeBeforeCreate = customerRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restCustomerMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(customer))
        ).andExpect(status().isBadRequest)

        // Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllCustomers() {
        // Initialize the database
        customerRepository.saveAndFlush(customer)

        // Get all the customerList
        restCustomerMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(customer.id?.toInt())))
            .andExpect(jsonPath("$.[*].firstName").value(hasItem(DEFAULT_FIRST_NAME)))
            .andExpect(jsonPath("$.[*].lastName").value(hasItem(DEFAULT_LAST_NAME)))
            .andExpect(jsonPath("$.[*].email").value(hasItem(DEFAULT_EMAIL)))
            .andExpect(jsonPath("$.[*].telephone").value(hasItem(DEFAULT_TELEPHONE)))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getCustomer() {
        // Initialize the database
        customerRepository.saveAndFlush(customer)

        val id = customer.id
        assertNotNull(id)

        // Get the customer
        restCustomerMockMvc.perform(get(ENTITY_API_URL_ID, customer.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(customer.id?.toInt()))
            .andExpect(jsonPath("$.firstName").value(DEFAULT_FIRST_NAME))
            .andExpect(jsonPath("$.lastName").value(DEFAULT_LAST_NAME))
            .andExpect(jsonPath("$.email").value(DEFAULT_EMAIL))
            .andExpect(jsonPath("$.telephone").value(DEFAULT_TELEPHONE))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingCustomer() {
        // Get the customer
        restCustomerMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewCustomer() {
        // Initialize the database
        customerRepository.saveAndFlush(customer)

        val databaseSizeBeforeUpdate = customerRepository.findAll().size

        // Update the customer
        val updatedCustomer = customerRepository.findById(customer.id).get()
        // Disconnect from session so that the updates on updatedCustomer are not directly saved in db
        em.detach(updatedCustomer)
        updatedCustomer.firstName = UPDATED_FIRST_NAME
        updatedCustomer.lastName = UPDATED_LAST_NAME
        updatedCustomer.email = UPDATED_EMAIL
        updatedCustomer.telephone = UPDATED_TELEPHONE

        restCustomerMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedCustomer.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedCustomer))
        ).andExpect(status().isOk)

        // Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate)
        val testCustomer = customerList[customerList.size - 1]
        assertThat(testCustomer.firstName).isEqualTo(UPDATED_FIRST_NAME)
        assertThat(testCustomer.lastName).isEqualTo(UPDATED_LAST_NAME)
        assertThat(testCustomer.email).isEqualTo(UPDATED_EMAIL)
        assertThat(testCustomer.telephone).isEqualTo(UPDATED_TELEPHONE)
    }

    @Test
    @Transactional
    fun putNonExistingCustomer() {
        val databaseSizeBeforeUpdate = customerRepository.findAll().size
        customer.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCustomerMockMvc.perform(
            put(ENTITY_API_URL_ID, customer.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(customer))
        )
            .andExpect(status().isBadRequest)

        // Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchCustomer() {
        val databaseSizeBeforeUpdate = customerRepository.findAll().size
        customer.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomerMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(customer))
        ).andExpect(status().isBadRequest)

        // Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamCustomer() {
        val databaseSizeBeforeUpdate = customerRepository.findAll().size
        customer.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomerMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(customer))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateCustomerWithPatch() {

        // Initialize the database
        customerRepository.saveAndFlush(customer)

        val databaseSizeBeforeUpdate = customerRepository.findAll().size

// Update the customer using partial update
        val partialUpdatedCustomer = Customer().apply {
            id = customer.id

            firstName = UPDATED_FIRST_NAME
            telephone = UPDATED_TELEPHONE
        }

        restCustomerMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedCustomer.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedCustomer))
        )
            .andExpect(status().isOk)

// Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate)
        val testCustomer = customerList.last()
        assertThat(testCustomer.firstName).isEqualTo(UPDATED_FIRST_NAME)
        assertThat(testCustomer.lastName).isEqualTo(DEFAULT_LAST_NAME)
        assertThat(testCustomer.email).isEqualTo(DEFAULT_EMAIL)
        assertThat(testCustomer.telephone).isEqualTo(UPDATED_TELEPHONE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateCustomerWithPatch() {

        // Initialize the database
        customerRepository.saveAndFlush(customer)

        val databaseSizeBeforeUpdate = customerRepository.findAll().size

// Update the customer using partial update
        val partialUpdatedCustomer = Customer().apply {
            id = customer.id

            firstName = UPDATED_FIRST_NAME
            lastName = UPDATED_LAST_NAME
            email = UPDATED_EMAIL
            telephone = UPDATED_TELEPHONE
        }

        restCustomerMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedCustomer.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedCustomer))
        )
            .andExpect(status().isOk)

// Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate)
        val testCustomer = customerList.last()
        assertThat(testCustomer.firstName).isEqualTo(UPDATED_FIRST_NAME)
        assertThat(testCustomer.lastName).isEqualTo(UPDATED_LAST_NAME)
        assertThat(testCustomer.email).isEqualTo(UPDATED_EMAIL)
        assertThat(testCustomer.telephone).isEqualTo(UPDATED_TELEPHONE)
    }

    @Throws(Exception::class)
    fun patchNonExistingCustomer() {
        val databaseSizeBeforeUpdate = customerRepository.findAll().size
        customer.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCustomerMockMvc.perform(
            patch(ENTITY_API_URL_ID, customer.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(customer))
        )
            .andExpect(status().isBadRequest)

        // Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchCustomer() {
        val databaseSizeBeforeUpdate = customerRepository.findAll().size
        customer.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomerMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(customer))
        )
            .andExpect(status().isBadRequest)

        // Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamCustomer() {
        val databaseSizeBeforeUpdate = customerRepository.findAll().size
        customer.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCustomerMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(customer))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Customer in the database
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteCustomer() {
        // Initialize the database
        customerRepository.saveAndFlush(customer)

        val databaseSizeBeforeDelete = customerRepository.findAll().size

        // Delete the customer
        restCustomerMockMvc.perform(
            delete(ENTITY_API_URL_ID, customer.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val customerList = customerRepository.findAll()
        assertThat(customerList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_FIRST_NAME = "AAAAAAAAAA"
        private const val UPDATED_FIRST_NAME = "BBBBBBBBBB"

        private const val DEFAULT_LAST_NAME = "AAAAAAAAAA"
        private const val UPDATED_LAST_NAME = "BBBBBBBBBB"

        private const val DEFAULT_EMAIL = "AAAAAAAAAA"
        private const val UPDATED_EMAIL = "BBBBBBBBBB"

        private const val DEFAULT_TELEPHONE = "AAAAAAAAAA"
        private const val UPDATED_TELEPHONE = "BBBBBBBBBB"

        private val ENTITY_API_URL: String = "/api/customers"
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
        fun createEntity(em: EntityManager): Customer {
            val customer = Customer(

                firstName = DEFAULT_FIRST_NAME,

                lastName = DEFAULT_LAST_NAME,

                email = DEFAULT_EMAIL,

                telephone = DEFAULT_TELEPHONE

            )

            return customer
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Customer {
            val customer = Customer(

                firstName = UPDATED_FIRST_NAME,

                lastName = UPDATED_LAST_NAME,

                email = UPDATED_EMAIL,

                telephone = UPDATED_TELEPHONE

            )

            return customer
        }
    }
}
