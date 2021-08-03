package com.library.publication.web.rest

import com.library.publication.domain.Category
import com.library.publication.repository.CategoryRepository
import com.library.publication.web.rest.errors.BadRequestAlertException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.Page
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

private const val ENTITY_NAME = "publicationCategory"
/**
 * REST controller for managing [com.library.publication.domain.Category].
 */
@RestController
@RequestMapping("/api")
@Transactional
class CategoryResource(
    private val categoryRepository: CategoryRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "publicationCategory"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /categories` : Create a new category.
     *
     * @param category the category to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new category, or with status `400 (Bad Request)` if the category has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/categories")
    fun createCategory(@Valid @RequestBody category: Category): ResponseEntity<Category> {
        log.debug("REST request to save Category : $category")
        if (category.id != null) {
            throw BadRequestAlertException(
                "A new category cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            )
        }
        val result = categoryRepository.save(category)
        return ResponseEntity.created(URI("/api/categories/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /categories/:id} : Updates an existing category.
     *
     * @param id the id of the category to save.
     * @param category the category to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated category,
     * or with status `400 (Bad Request)` if the category is not valid,
     * or with status `500 (Internal Server Error)` if the category couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/categories/{id}")
    fun updateCategory(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody category: Category
    ): ResponseEntity<Category> {
        log.debug("REST request to update Category : {}, {}", id, category)
        if (category.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, category.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!categoryRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = categoryRepository.save(category)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    category.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /categories/:id} : Partial updates given fields of an existing category, field will ignore if it is null
     *
     * @param id the id of the category to save.
     * @param category the category to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated category,
     * or with status {@code 400 (Bad Request)} if the category is not valid,
     * or with status {@code 404 (Not Found)} if the category is not found,
     * or with status {@code 500 (Internal Server Error)} if the category couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/categories/{id}"], consumes = ["application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateCategory(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody category: Category
    ): ResponseEntity<Category> {
        log.debug("REST request to partial update Category partially : {}, {}", id, category)
        if (category.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, category.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!categoryRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = categoryRepository.findById(category.id)
            .map {

                if (category.description != null) {
                    it.description = category.description
                }
                if (category.sortOrder != null) {
                    it.sortOrder = category.sortOrder
                }
                if (category.dateAdded != null) {
                    it.dateAdded = category.dateAdded
                }
                if (category.dateModified != null) {
                    it.dateModified = category.dateModified
                }
                if (category.status != null) {
                    it.status = category.status
                }

                it
            }
            .map { categoryRepository.save(it) }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, category.id.toString())
        )
    }

    /**
     * `GET  /categories` : get all the categories.
     *
     * @param pageable the pagination information.
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the [ResponseEntity] with status `200 (OK)` and the list of categories in body.
     */
    @GetMapping("/categories")
    fun getAllCategories(pageable: Pageable, @RequestParam(required = false, defaultValue = "false") eagerload: Boolean): ResponseEntity<List<Category>> {
        log.debug("REST request to get a page of Categories")
        val page: Page<Category> = if (eagerload) {
            categoryRepository.findAllWithEagerRelationships(pageable)
        } else {
            categoryRepository.findAll(pageable)
        }
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /categories/:id` : get the "id" category.
     *
     * @param id the id of the category to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the category, or with status `404 (Not Found)`.
     */
    @GetMapping("/categories/{id}")
    fun getCategory(@PathVariable id: Long): ResponseEntity<Category> {
        log.debug("REST request to get Category : $id")
        val category = categoryRepository.findOneWithEagerRelationships(id)
        return ResponseUtil.wrapOrNotFound(category)
    }
    /**
     *  `DELETE  /categories/:id` : delete the "id" category.
     *
     * @param id the id of the category to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/categories/{id}")
    fun deleteCategory(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Category : $id")

        categoryRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
