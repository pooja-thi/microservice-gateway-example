package com.library.publication.web.rest

import com.library.publication.domain.Book
import com.library.publication.repository.BookRepository
import com.library.publication.web.rest.errors.BadRequestAlertException
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

private const val ENTITY_NAME = "publicationBook"
/**
 * REST controller for managing [com.library.publication.domain.Book].
 */
@RestController
@RequestMapping("/api")
@Transactional
class BookResource(
    private val bookRepository: BookRepository
) {

    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val ENTITY_NAME = "publicationBook"
    }

    @Value("\${jhipster.clientApp.name}")
    private var applicationName: String? = null

    /**
     * `POST  /books` : Create a new book.
     *
     * @param book the book to create.
     * @return the [ResponseEntity] with status `201 (Created)` and with body the new book, or with status `400 (Bad Request)` if the book has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/books")
    fun createBook(@Valid @RequestBody book: Book): ResponseEntity<Book> {
        log.debug("REST request to save Book : $book")
        if (book.id != null) {
            throw BadRequestAlertException(
                "A new book cannot already have an ID",
                ENTITY_NAME,
                "idexists"
            )
        }
        val result = bookRepository.save(book)
        return ResponseEntity.created(URI("/api/books/${result.id}"))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.id.toString()))
            .body(result)
    }

    /**
     * {@code PUT  /books/:id} : Updates an existing book.
     *
     * @param id the id of the book to save.
     * @param book the book to update.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the updated book,
     * or with status `400 (Bad Request)` if the book is not valid,
     * or with status `500 (Internal Server Error)` if the book couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/books/{id}")
    fun updateBook(
        @PathVariable(value = "id", required = false) id: Long,
        @Valid @RequestBody book: Book
    ): ResponseEntity<Book> {
        log.debug("REST request to update Book : {}, {}", id, book)
        if (book.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }

        if (!Objects.equals(id, book.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!bookRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = bookRepository.save(book)
        return ResponseEntity.ok()
            .headers(
                HeaderUtil.createEntityUpdateAlert(
                    applicationName,
                    true,
                    ENTITY_NAME,
                    book.id.toString()
                )
            )
            .body(result)
    }

    /**
     * {@code PATCH  /books/:id} : Partial updates given fields of an existing book, field will ignore if it is null
     *
     * @param id the id of the book to save.
     * @param book the book to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated book,
     * or with status {@code 400 (Bad Request)} if the book is not valid,
     * or with status {@code 404 (Not Found)} if the book is not found,
     * or with status {@code 500 (Internal Server Error)} if the book couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = ["/books/{id}"], consumes = ["application/merge-patch+json"])
    @Throws(URISyntaxException::class)
    fun partialUpdateBook(
        @PathVariable(value = "id", required = false) id: Long,
        @NotNull @RequestBody book: Book
    ): ResponseEntity<Book> {
        log.debug("REST request to partial update Book partially : {}, {}", id, book)
        if (book.id == null) {
            throw BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull")
        }
        if (!Objects.equals(id, book.id)) {
            throw BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid")
        }

        if (!bookRepository.existsById(id)) {
            throw BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound")
        }

        val result = bookRepository.findById(book.id)
            .map {

                if (book.title != null) {
                    it.title = book.title
                }
                if (book.author != null) {
                    it.author = book.author
                }
                if (book.keywords != null) {
                    it.keywords = book.keywords
                }
                if (book.description != null) {
                    it.description = book.description
                }
                if (book.rating != null) {
                    it.rating = book.rating
                }
                if (book.dateAdded != null) {
                    it.dateAdded = book.dateAdded
                }
                if (book.dateModified != null) {
                    it.dateModified = book.dateModified
                }
                if (book.image != null) {
                    it.image = book.image
                }
                if (book.imageContentType != null) {
                    it.imageContentType = book.imageContentType
                }

                it
            }
            .map { bookRepository.save(it) }

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, book.id.toString())
        )
    }

    /**
     * `GET  /books` : get all the books.
     *
     * @param pageable the pagination information.

     * @return the [ResponseEntity] with status `200 (OK)` and the list of books in body.
     */
    @GetMapping("/books")
    fun getAllBooks(pageable: Pageable): ResponseEntity<List<Book>> {
        log.debug("REST request to get a page of Books")
        val page = bookRepository.findAll(pageable)
        val headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page)
        return ResponseEntity.ok().headers(headers).body(page.content)
    }

    /**
     * `GET  /books/:id` : get the "id" book.
     *
     * @param id the id of the book to retrieve.
     * @return the [ResponseEntity] with status `200 (OK)` and with body the book, or with status `404 (Not Found)`.
     */
    @GetMapping("/books/{id}")
    fun getBook(@PathVariable id: Long): ResponseEntity<Book> {
        log.debug("REST request to get Book : $id")
        val book = bookRepository.findById(id)
        return ResponseUtil.wrapOrNotFound(book)
    }
    /**
     *  `DELETE  /books/:id` : delete the "id" book.
     *
     * @param id the id of the book to delete.
     * @return the [ResponseEntity] with status `204 (NO_CONTENT)`.
     */
    @DeleteMapping("/books/{id}")
    fun deleteBook(@PathVariable id: Long): ResponseEntity<Void> {
        log.debug("REST request to delete Book : $id")

        bookRepository.deleteById(id)
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString())).build()
    }
}
