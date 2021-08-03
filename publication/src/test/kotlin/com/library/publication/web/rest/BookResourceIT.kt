package com.library.publication.web.rest

import com.library.publication.IntegrationTest
import com.library.publication.domain.Book
import com.library.publication.repository.BookRepository
import com.library.publication.web.rest.errors.ExceptionTranslator
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
import org.springframework.util.Base64Utils
import org.springframework.validation.Validator
import java.time.LocalDate
import java.time.ZoneId
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [BookResource] REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class BookResourceIT {
    @Autowired
    private lateinit var bookRepository: BookRepository

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
    private lateinit var restBookMockMvc: MockMvc

    private lateinit var book: Book

    @BeforeEach
    fun initTest() {
        book = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createBook() {
        val databaseSizeBeforeCreate = bookRepository.findAll().size

        // Create the Book
        restBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isCreated)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeCreate + 1)
        val testBook = bookList[bookList.size - 1]

        assertThat(testBook.title).isEqualTo(DEFAULT_TITLE)
        assertThat(testBook.author).isEqualTo(DEFAULT_AUTHOR)
        assertThat(testBook.keywords).isEqualTo(DEFAULT_KEYWORDS)
        assertThat(testBook.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testBook.rating).isEqualTo(DEFAULT_RATING)
        assertThat(testBook.dateAdded).isEqualTo(DEFAULT_DATE_ADDED)
        assertThat(testBook.dateModified).isEqualTo(DEFAULT_DATE_MODIFIED)
        assertThat(testBook.image).isEqualTo(DEFAULT_IMAGE)
        assertThat(testBook.imageContentType).isEqualTo(DEFAULT_IMAGE_CONTENT_TYPE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createBookWithExistingId() {
        // Create the Book with an existing ID
        book.id = 1L

        val databaseSizeBeforeCreate = bookRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkTitleIsRequired() {
        val databaseSizeBeforeTest = bookRepository.findAll().size
        // set the field null
        book.title = null

        // Create the Book, which fails.

        restBookMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllBooks() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        // Get all the bookList
        restBookMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(book.id?.toInt())))
            .andExpect(jsonPath("$.[*].title").value(hasItem(DEFAULT_TITLE)))
            .andExpect(jsonPath("$.[*].author").value(hasItem(DEFAULT_AUTHOR)))
            .andExpect(jsonPath("$.[*].keywords").value(hasItem(DEFAULT_KEYWORDS)))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION.toString())))
            .andExpect(jsonPath("$.[*].rating").value(hasItem(DEFAULT_RATING)))
            .andExpect(jsonPath("$.[*].dateAdded").value(hasItem(DEFAULT_DATE_ADDED.toString())))
            .andExpect(jsonPath("$.[*].dateModified").value(hasItem(DEFAULT_DATE_MODIFIED.toString())))
            .andExpect(jsonPath("$.[*].imageContentType").value(hasItem(DEFAULT_IMAGE_CONTENT_TYPE)))
            .andExpect(jsonPath("$.[*].image").value(hasItem(Base64Utils.encodeToString(DEFAULT_IMAGE))))
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getBook() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        val id = book.id
        assertNotNull(id)

        // Get the book
        restBookMockMvc.perform(get(ENTITY_API_URL_ID, book.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(book.id?.toInt()))
            .andExpect(jsonPath("$.title").value(DEFAULT_TITLE))
            .andExpect(jsonPath("$.author").value(DEFAULT_AUTHOR))
            .andExpect(jsonPath("$.keywords").value(DEFAULT_KEYWORDS))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION.toString()))
            .andExpect(jsonPath("$.rating").value(DEFAULT_RATING))
            .andExpect(jsonPath("$.dateAdded").value(DEFAULT_DATE_ADDED.toString()))
            .andExpect(jsonPath("$.dateModified").value(DEFAULT_DATE_MODIFIED.toString()))
            .andExpect(jsonPath("$.imageContentType").value(DEFAULT_IMAGE_CONTENT_TYPE))
            .andExpect(jsonPath("$.image").value(Base64Utils.encodeToString(DEFAULT_IMAGE)))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingBook() {
        // Get the book
        restBookMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewBook() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeUpdate = bookRepository.findAll().size

        // Update the book
        val updatedBook = bookRepository.findById(book.id).get()
        // Disconnect from session so that the updates on updatedBook are not directly saved in db
        em.detach(updatedBook)
        updatedBook.title = UPDATED_TITLE
        updatedBook.author = UPDATED_AUTHOR
        updatedBook.keywords = UPDATED_KEYWORDS
        updatedBook.description = UPDATED_DESCRIPTION
        updatedBook.rating = UPDATED_RATING
        updatedBook.dateAdded = UPDATED_DATE_ADDED
        updatedBook.dateModified = UPDATED_DATE_MODIFIED
        updatedBook.image = UPDATED_IMAGE
        updatedBook.imageContentType = UPDATED_IMAGE_CONTENT_TYPE

        restBookMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedBook.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedBook))
        ).andExpect(status().isOk)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList[bookList.size - 1]
        assertThat(testBook.title).isEqualTo(UPDATED_TITLE)
        assertThat(testBook.author).isEqualTo(UPDATED_AUTHOR)
        assertThat(testBook.keywords).isEqualTo(UPDATED_KEYWORDS)
        assertThat(testBook.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testBook.rating).isEqualTo(UPDATED_RATING)
        assertThat(testBook.dateAdded).isEqualTo(UPDATED_DATE_ADDED)
        assertThat(testBook.dateModified).isEqualTo(UPDATED_DATE_MODIFIED)
        assertThat(testBook.image).isEqualTo(UPDATED_IMAGE)
        assertThat(testBook.imageContentType).isEqualTo(UPDATED_IMAGE_CONTENT_TYPE)
    }

    @Test
    @Transactional
    fun putNonExistingBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            put(ENTITY_API_URL_ID, book.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        ).andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateBookWithPatch() {

        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeUpdate = bookRepository.findAll().size

// Update the book using partial update
        val partialUpdatedBook = Book().apply {
            id = book.id

            keywords = UPDATED_KEYWORDS
            description = UPDATED_DESCRIPTION
            rating = UPDATED_RATING
            dateModified = UPDATED_DATE_MODIFIED
            image = UPDATED_IMAGE
            imageContentType = UPDATED_IMAGE_CONTENT_TYPE
        }

        restBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedBook.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedBook))
        )
            .andExpect(status().isOk)

// Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList.last()
        assertThat(testBook.title).isEqualTo(DEFAULT_TITLE)
        assertThat(testBook.author).isEqualTo(DEFAULT_AUTHOR)
        assertThat(testBook.keywords).isEqualTo(UPDATED_KEYWORDS)
        assertThat(testBook.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testBook.rating).isEqualTo(UPDATED_RATING)
        assertThat(testBook.dateAdded).isEqualTo(DEFAULT_DATE_ADDED)
        assertThat(testBook.dateModified).isEqualTo(UPDATED_DATE_MODIFIED)
        assertThat(testBook.image).isEqualTo(UPDATED_IMAGE)
        assertThat(testBook.imageContentType).isEqualTo(UPDATED_IMAGE_CONTENT_TYPE)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateBookWithPatch() {

        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeUpdate = bookRepository.findAll().size

// Update the book using partial update
        val partialUpdatedBook = Book().apply {
            id = book.id

            title = UPDATED_TITLE
            author = UPDATED_AUTHOR
            keywords = UPDATED_KEYWORDS
            description = UPDATED_DESCRIPTION
            rating = UPDATED_RATING
            dateAdded = UPDATED_DATE_ADDED
            dateModified = UPDATED_DATE_MODIFIED
            image = UPDATED_IMAGE
            imageContentType = UPDATED_IMAGE_CONTENT_TYPE
        }

        restBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedBook.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedBook))
        )
            .andExpect(status().isOk)

// Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
        val testBook = bookList.last()
        assertThat(testBook.title).isEqualTo(UPDATED_TITLE)
        assertThat(testBook.author).isEqualTo(UPDATED_AUTHOR)
        assertThat(testBook.keywords).isEqualTo(UPDATED_KEYWORDS)
        assertThat(testBook.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testBook.rating).isEqualTo(UPDATED_RATING)
        assertThat(testBook.dateAdded).isEqualTo(UPDATED_DATE_ADDED)
        assertThat(testBook.dateModified).isEqualTo(UPDATED_DATE_MODIFIED)
        assertThat(testBook.image).isEqualTo(UPDATED_IMAGE)
        assertThat(testBook.imageContentType).isEqualTo(UPDATED_IMAGE_CONTENT_TYPE)
    }

    @Throws(Exception::class)
    fun patchNonExistingBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, book.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isBadRequest)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamBook() {
        val databaseSizeBeforeUpdate = bookRepository.findAll().size
        book.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restBookMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(book))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Book in the database
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteBook() {
        // Initialize the database
        bookRepository.saveAndFlush(book)

        val databaseSizeBeforeDelete = bookRepository.findAll().size

        // Delete the book
        restBookMockMvc.perform(
            delete(ENTITY_API_URL_ID, book.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val bookList = bookRepository.findAll()
        assertThat(bookList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_TITLE = "AAAAAAAAAA"
        private const val UPDATED_TITLE = "BBBBBBBBBB"

        private const val DEFAULT_AUTHOR = "AAAAAAAAAA"
        private const val UPDATED_AUTHOR = "BBBBBBBBBB"

        private const val DEFAULT_KEYWORDS = "AAAAAAAAAA"
        private const val UPDATED_KEYWORDS = "BBBBBBBBBB"

        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        private const val DEFAULT_RATING: Int = 1
        private const val UPDATED_RATING: Int = 2

        private val DEFAULT_DATE_ADDED: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_DATE_ADDED: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private val DEFAULT_DATE_MODIFIED: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_DATE_MODIFIED: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private val DEFAULT_IMAGE: ByteArray = createByteArray(1, "0")
        private val UPDATED_IMAGE: ByteArray = createByteArray(1, "1")
        private const val DEFAULT_IMAGE_CONTENT_TYPE: String = "image/jpg"
        private const val UPDATED_IMAGE_CONTENT_TYPE: String = "image/png"

        private val ENTITY_API_URL: String = "/api/books"
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
        fun createEntity(em: EntityManager): Book {
            val book = Book(

                title = DEFAULT_TITLE,

                author = DEFAULT_AUTHOR,

                keywords = DEFAULT_KEYWORDS,

                description = DEFAULT_DESCRIPTION,

                rating = DEFAULT_RATING,

                dateAdded = DEFAULT_DATE_ADDED,

                dateModified = DEFAULT_DATE_MODIFIED,

                image = DEFAULT_IMAGE,
                imageContentType = DEFAULT_IMAGE_CONTENT_TYPE

            )

            return book
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Book {
            val book = Book(

                title = UPDATED_TITLE,

                author = UPDATED_AUTHOR,

                keywords = UPDATED_KEYWORDS,

                description = UPDATED_DESCRIPTION,

                rating = UPDATED_RATING,

                dateAdded = UPDATED_DATE_ADDED,

                dateModified = UPDATED_DATE_MODIFIED,

                image = UPDATED_IMAGE,
                imageContentType = UPDATED_IMAGE_CONTENT_TYPE

            )

            return book
        }
    }
}
