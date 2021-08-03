package com.library.user.web.rest

import com.library.user.domain.Address
import com.library.user.repository.AddressRepository
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
import javax.validation.Valid
import javax.validation.constraints.NotNull

private const val ENTITY_NAME = "userAddress"
/**
 * REST controller for managing [com.library.user.domain.Address].
 */
@RestController
@RequestMapping("/api")
@Transactional
class AddressResource(
    private val addressRepository: AddressRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "userAddress"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /addresses` : Create a new address.
     *
     * @param address the address to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new address, or with status `400 (Bad Request)` if the address has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/addresses")
    fun createAddress(@Valid @RequestBody address: Address): ResponseEntity<Address> {
        log.debug("REST request to save Address : $address")
        if (address.id != null) {
            throw BadRequestAlertException(
                "A new address cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            )
        }
        val result = addressRepository.save(address)
        return ResponseEntity.created(URI("/api/addresses/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /addresses/:id} : Updates an existing address.
     *
     * @param id the id of the address to save.
     * @param address the address to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated address,
     * or with status `400 (Bad Request)` if the address is not valid,
     * or with status `500 (Internal Server Error)` if the address couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/addresses/{id}")
    fun updateAddress(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody address: Address
    ): ResponseEntity<Address> {
        log.debug("REST request to update Address : {}, {}", id, address)
        if (address.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, address.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!addressRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = addressRepository.save(address)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    address.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /addresses/:id} : Partial updates given fields of an existing address, field will ignore if it is null
     *
     * @param id the id of the address to save.
     * @param address the address to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated address,
     * or with status {@code 400 (Bad Request)} if the address is not valid,
     * or with status {@code 404 (Not Found)} if the address is not found,
     * or with status {@code 500 (Internal Server Error)} if the address couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/addresses/{id}"], consumes = ["application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateAddress(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody address: Address
    ): ResponseEntity<Address> {
        log.debug("REST request to partial update Address partially : {}, {}", id, address)
        if (address.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, address.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!addressRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = addressRepository.findById(address.id)
            .map {

                if (address.address1 != null) {
                    it.address1 = address.address1
                }
                if (address.address2 != null) {
                    it.address2 = address.address2
                }
                if (address.city != null) {
                    it.city = address.city
                }
                if (address.postcode != null) {
                    it.postcode = address.postcode
                }
                if (address.country != null) {
                    it.country = address.country
                }

                it
            }
            .map { addressRepository.save(it) }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, address.id.toString())
        )
    }

    /**
     * `GET  /addresses` : get all the addresses.
     *
     * @param pageable the pagination information.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of addresses in body.
     */
    @GetMapping("/addresses")
    fun getAllAddresses(pageable: Pageable): ResponseEntity<List<Address>> {
        log.debug("REST request to get a page of Addresses")
        val page = addressRepository.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /addresses/:id` : get the "id" address.
     *
     * @param id the id of the address to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the address, or with status `404 (Not Found)`.
     */
    @GetMapping("/addresses/{id}")
    fun getAddress(@PathVariable id: Long): ResponseEntity<Address> {
        log.debug("REST request to get Address : $id")
        val address = addressRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(address)
    }
    /**
     *  `DELETE  /addresses/:id` : delete the "id" address.
     *
     * @param id the id of the address to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/addresses/{id}")
    fun deleteAddress(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Address : $id")

        addressRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
