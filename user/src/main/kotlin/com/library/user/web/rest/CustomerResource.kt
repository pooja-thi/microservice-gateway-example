package com.library.user.web.rest

import com.library.user.domain.Customer
import com.library.user.repository.CustomerRepository
import com.library.user.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import tech.jhipster.web.util.HeaderUtil
import tech.jhipster.web.util.PaginationUtil
import tech.jhipster.web.util.ResponseUtil
import java.net.URI
import java.net.URISyntaxException
import java.util.Objects

private const val ENTITY_NAME = "userCustomer"
/**
 * REST controller for managing [com.library.user.domain.Customer].
 */
@RestController
@RequestMapping("/api")
@Transactional
class CustomerResource(
    private val customerRepository: CustomerRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "userCustomer"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /customers` : Create a new customer.
     *
     * @param customer the customer to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new customer, or with status `400 (Bad Request)` if the customer has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/customers")
    fun createCustomer(@RequestBody customer: Customer): ResponseEntity<Customer> {
        log.debug("REST request to save Customer : $customer")
        if (customer.id != null) {
            throw BadRequestAlertException(
                "A new customer cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            )
        }
        val result = customerRepository.save(customer)
        return ResponseEntity.created(URI("/api/customers/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /customers/:id} : Updates an existing customer.
     *
     * @param id the id of the customer to save.
     * @param customer the customer to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated customer,
     * or with status `400 (Bad Request)` if the customer is not valid,
     * or with status `500 (Internal Server Error)` if the customer couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/customers/{id}")
    fun updateCustomer(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody customer: Customer
    ): ResponseEntity<Customer> {
        log.debug("REST request to update Customer : {}, {}", id, customer)
        if (customer.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, customer.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!customerRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = customerRepository.save(customer)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    customer.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /customers/:id} : Partial updates given fields of an existing customer, field will ignore if it is null
     *
     * @param id the id of the customer to save.
     * @param customer the customer to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated customer,
     * or with status {@code 400 (Bad Request)} if the customer is not valid,
     * or with status {@code 404 (Not Found)} if the customer is not found,
     * or with status {@code 500 (Internal Server Error)} if the customer couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/customers/{id}"], consumes = ["application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateCustomer(
        @PathVariable(value = "id", required = false) id: Long,
        @RequestBody customer: Customer
    ): ResponseEntity<Customer> {
        log.debug("REST request to partial update Customer partially : {}, {}", id, customer)
        if (customer.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, customer.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!customerRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = customerRepository.findById(customer.id)
            .map {

                if (customer.firstName != null) {
                    it.firstName = customer.firstName
                }
                if (customer.lastName != null) {
                    it.lastName = customer.lastName
                }
                if (customer.email != null) {
                    it.email = customer.email
                }
                if (customer.telephone != null) {
                    it.telephone = customer.telephone
                }

                it
            }
            .map { customerRepository.save(it) }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, customer.id.toString())
        )
    }

    /**
     * `GET  /customers` : get all the customers.
     *
     * @param pageable the pagination information.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of customers in body.
     */
    @GetMapping("/customers")
    fun getAllCustomers(pageable: Pageable): ResponseEntity<List<Customer>> {
        log.debug("REST request to get a page of Customers")
        val page = customerRepository.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /customers/:id` : get the "id" customer.
     *
     * @param id the id of the customer to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the customer, or with status `404 (Not Found)`.
     */
    @GetMapping("/customers/{id}")
    fun getCustomer(@PathVariable id: Long): ResponseEntity<Customer> {
        log.debug("REST request to get Customer : $id")
        val customer = customerRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(customer)
    }
    /**
     *  `DELETE  /customers/:id` : delete the "id" customer.
     *
     * @param id the id of the customer to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/customers/{id}")
    fun deleteCustomer(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Customer : $id")

        customerRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
