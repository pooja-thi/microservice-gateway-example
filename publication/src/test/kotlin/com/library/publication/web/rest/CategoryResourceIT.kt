package com.library.publication.web.rest

import com.library.publication.IntegrationTest
import com.library.publication.domain.Category
import com.library.publication.domain.enumeration.CategoryStatus
import com.library.publication.repository.CategoryRepository
import com.library.publication.web.rest.errors.ExceptionTranslator
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers.hasItem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.Extensions
import org.mockito.ArgumentMatchers.*
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.data.domain.PageImpl
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
import java.time.LocalDate
import java.time.ZoneId
import java.util.Random
import java.util.concurrent.atomic.AtomicLong
import javax.persistence.EntityManager
import kotlin.test.assertNotNull

/**
 * Integration tests for the [CategoryResource] REST controller.
 */
@IntegrationTest
@Extensions(
    ExtendWith(MockitoExtension::class)
)
@AutoConfigureMockMvc
@WithMockUser
class CategoryResourceIT {
    @Autowired
    private lateinit var categoryRepository: CategoryRepository

    @Mock
    private lateinit var categoryRepositoryMock: CategoryRepository

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
    private lateinit var restCategoryMockMvc: MockMvc

    private lateinit var category: Category

    @BeforeEach
    fun initTest() {
        category = createEntity(em)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCategory() {
        val databaseSizeBeforeCreate = categoryRepository.findAll().size

        // Create the Category
        restCategoryMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(category))
        ).andExpect(status().isCreated)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate + 1)
        val testCategory = categoryList[categoryList.size - 1]

        assertThat(testCategory.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testCategory.sortOrder).isEqualTo(DEFAULT_SORT_ORDER)
        assertThat(testCategory.dateAdded).isEqualTo(DEFAULT_DATE_ADDED)
        assertThat(testCategory.dateModified).isEqualTo(DEFAULT_DATE_MODIFIED)
        assertThat(testCategory.status).isEqualTo(DEFAULT_STATUS)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun createCategoryWithExistingId() {
        // Create the Category with an existing ID
        category.id = 1L

        val databaseSizeBeforeCreate = categoryRepository.findAll().size

        // An entity with an existing ID cannot be created, so this API call must fail
        restCategoryMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(category))
        ).andExpect(status().isBadRequest)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeCreate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun checkDescriptionIsRequired() {
        val databaseSizeBeforeTest = categoryRepository.findAll().size
        // set the field null
        category.description = null

        // Create the Category, which fails.

        restCategoryMockMvc.perform(
            post(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(category))
        ).andExpect(status().isBadRequest)

        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeTest)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getAllCategories() {
        // Initialize the database
        categoryRepository.saveAndFlush(category)

        // Get all the categoryList
        restCategoryMockMvc.perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(category.id?.toInt())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)))
            .andExpect(jsonPath("$.[*].sortOrder").value(hasItem(DEFAULT_SORT_ORDER)))
            .andExpect(jsonPath("$.[*].dateAdded").value(hasItem(DEFAULT_DATE_ADDED.toString())))
            .andExpect(jsonPath("$.[*].dateModified").value(hasItem(DEFAULT_DATE_MODIFIED.toString())))
            .andExpect(jsonPath("$.[*].status").value(hasItem(DEFAULT_STATUS.toString())))
    }

    @Suppress("unchecked")
    @Throws(Exception::class)
    fun getAllCategoriesWithEagerRelationshipsIsEnabled() {
        val categoryResource = CategoryResource(categoryRepositoryMock)
        `when`(categoryRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        restCategoryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true"))
            .andExpect(status().isOk)

        verify(categoryRepositoryMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Suppress("unchecked")
    @Throws(Exception::class)
    fun getAllCategoriesWithEagerRelationshipsIsNotEnabled() {
        val categoryResource = CategoryResource(categoryRepositoryMock)
        `when`(categoryRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(PageImpl(mutableListOf()))

        restCategoryMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true"))
            .andExpect(status().isOk)

        verify(categoryRepositoryMock, times(1)).findAllWithEagerRelationships(any())
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun getCategory() {
        // Initialize the database
        categoryRepository.saveAndFlush(category)

        val id = category.id
        assertNotNull(id)

        // Get the category
        restCategoryMockMvc.perform(get(ENTITY_API_URL_ID, category.id))
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(category.id?.toInt()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION))
            .andExpect(jsonPath("$.sortOrder").value(DEFAULT_SORT_ORDER))
            .andExpect(jsonPath("$.dateAdded").value(DEFAULT_DATE_ADDED.toString()))
            .andExpect(jsonPath("$.dateModified").value(DEFAULT_DATE_MODIFIED.toString()))
            .andExpect(jsonPath("$.status").value(DEFAULT_STATUS.toString()))
    }
    @Test
    @Transactional
    @Throws(Exception::class)
    fun getNonExistingCategory() {
        // Get the category
        restCategoryMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE))
            .andExpect(status().isNotFound)
    }
    @Test
    @Transactional
    fun putNewCategory() {
        // Initialize the database
        categoryRepository.saveAndFlush(category)

        val databaseSizeBeforeUpdate = categoryRepository.findAll().size

        // Update the category
        val updatedCategory = categoryRepository.findById(category.id).get()
        // Disconnect from session so that the updates on updatedCategory are not directly saved in db
        em.detach(updatedCategory)
        updatedCategory.description = UPDATED_DESCRIPTION
        updatedCategory.sortOrder = UPDATED_SORT_ORDER
        updatedCategory.dateAdded = UPDATED_DATE_ADDED
        updatedCategory.dateModified = UPDATED_DATE_MODIFIED
        updatedCategory.status = UPDATED_STATUS

        restCategoryMockMvc.perform(
            put(ENTITY_API_URL_ID, updatedCategory.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(updatedCategory))
        ).andExpect(status().isOk)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
        val testCategory = categoryList[categoryList.size - 1]
        assertThat(testCategory.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testCategory.sortOrder).isEqualTo(UPDATED_SORT_ORDER)
        assertThat(testCategory.dateAdded).isEqualTo(UPDATED_DATE_ADDED)
        assertThat(testCategory.dateModified).isEqualTo(UPDATED_DATE_MODIFIED)
        assertThat(testCategory.status).isEqualTo(UPDATED_STATUS)
    }

    @Test
    @Transactional
    fun putNonExistingCategory() {
        val databaseSizeBeforeUpdate = categoryRepository.findAll().size
        category.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategoryMockMvc.perform(
            put(ENTITY_API_URL_ID, category.id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(category))
        )
            .andExpect(status().isBadRequest)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithIdMismatchCategory() {
        val databaseSizeBeforeUpdate = categoryRepository.findAll().size
        category.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc.perform(
            put(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(category))
        ).andExpect(status().isBadRequest)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun putWithMissingIdPathParamCategory() {
        val databaseSizeBeforeUpdate = categoryRepository.findAll().size
        category.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc.perform(
            put(ENTITY_API_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(category))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun partialUpdateCategoryWithPatch() {

        // Initialize the database
        categoryRepository.saveAndFlush(category)

        val databaseSizeBeforeUpdate = categoryRepository.findAll().size

// Update the category using partial update
        val partialUpdatedCategory = Category().apply {
            id = category.id

            sortOrder = UPDATED_SORT_ORDER
            dateAdded = UPDATED_DATE_ADDED
            dateModified = UPDATED_DATE_MODIFIED
        }

        restCategoryMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedCategory.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedCategory))
        )
            .andExpect(status().isOk)

// Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
        val testCategory = categoryList.last()
        assertThat(testCategory.description).isEqualTo(DEFAULT_DESCRIPTION)
        assertThat(testCategory.sortOrder).isEqualTo(UPDATED_SORT_ORDER)
        assertThat(testCategory.dateAdded).isEqualTo(UPDATED_DATE_ADDED)
        assertThat(testCategory.dateModified).isEqualTo(UPDATED_DATE_MODIFIED)
        assertThat(testCategory.status).isEqualTo(DEFAULT_STATUS)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun fullUpdateCategoryWithPatch() {

        // Initialize the database
        categoryRepository.saveAndFlush(category)

        val databaseSizeBeforeUpdate = categoryRepository.findAll().size

// Update the category using partial update
        val partialUpdatedCategory = Category().apply {
            id = category.id

            description = UPDATED_DESCRIPTION
            sortOrder = UPDATED_SORT_ORDER
            dateAdded = UPDATED_DATE_ADDED
            dateModified = UPDATED_DATE_MODIFIED
            status = UPDATED_STATUS
        }

        restCategoryMockMvc.perform(
            patch(ENTITY_API_URL_ID, partialUpdatedCategory.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(partialUpdatedCategory))
        )
            .andExpect(status().isOk)

// Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
        val testCategory = categoryList.last()
        assertThat(testCategory.description).isEqualTo(UPDATED_DESCRIPTION)
        assertThat(testCategory.sortOrder).isEqualTo(UPDATED_SORT_ORDER)
        assertThat(testCategory.dateAdded).isEqualTo(UPDATED_DATE_ADDED)
        assertThat(testCategory.dateModified).isEqualTo(UPDATED_DATE_MODIFIED)
        assertThat(testCategory.status).isEqualTo(UPDATED_STATUS)
    }

    @Throws(Exception::class)
    fun patchNonExistingCategory() {
        val databaseSizeBeforeUpdate = categoryRepository.findAll().size
        category.id = count.incrementAndGet()

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCategoryMockMvc.perform(
            patch(ENTITY_API_URL_ID, category.id).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(category))
        )
            .andExpect(status().isBadRequest)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithIdMismatchCategory() {
        val databaseSizeBeforeUpdate = categoryRepository.findAll().size
        category.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc.perform(
            patch(ENTITY_API_URL_ID, count.incrementAndGet()).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(category))
        )
            .andExpect(status().isBadRequest)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun patchWithMissingIdPathParamCategory() {
        val databaseSizeBeforeUpdate = categoryRepository.findAll().size
        category.id = count.incrementAndGet()

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCategoryMockMvc.perform(
            patch(ENTITY_API_URL).with(csrf())
                .contentType("application/merge-patch+json")
                .content(convertObjectToJsonBytes(category))
        )
            .andExpect(status().isMethodNotAllowed)

        // Validate the Category in the database
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeUpdate)
    }

    @Test
    @Transactional
    @Throws(Exception::class)
    fun deleteCategory() {
        // Initialize the database
        categoryRepository.saveAndFlush(category)

        val databaseSizeBeforeDelete = categoryRepository.findAll().size

        // Delete the category
        restCategoryMockMvc.perform(
            delete(ENTITY_API_URL_ID, category.id).with(csrf())
                .accept(MediaType.APPLICATION_JSON)
        ).andExpect(status().isNoContent)

        // Validate the database contains one less item
        val categoryList = categoryRepository.findAll()
        assertThat(categoryList).hasSize(databaseSizeBeforeDelete - 1)
    }

    companion object {

        private const val DEFAULT_DESCRIPTION = "AAAAAAAAAA"
        private const val UPDATED_DESCRIPTION = "BBBBBBBBBB"

        private const val DEFAULT_SORT_ORDER: Int = 1
        private const val UPDATED_SORT_ORDER: Int = 2

        private val DEFAULT_DATE_ADDED: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_DATE_ADDED: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private val DEFAULT_DATE_MODIFIED: LocalDate = LocalDate.ofEpochDay(0L)
        private val UPDATED_DATE_MODIFIED: LocalDate = LocalDate.now(ZoneId.systemDefault())

        private val DEFAULT_STATUS: CategoryStatus = CategoryStatus.AVAILABLE
        private val UPDATED_STATUS: CategoryStatus = CategoryStatus.BORROWED

        private val ENTITY_API_URL: String = "/api/categories"
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
        fun createEntity(em: EntityManager): Category {
            val category = Category(

                description = DEFAULT_DESCRIPTION,

                sortOrder = DEFAULT_SORT_ORDER,

                dateAdded = DEFAULT_DATE_ADDED,

                dateModified = DEFAULT_DATE_MODIFIED,

                status = DEFAULT_STATUS

            )

            return category
        }

        /**
         * Create an updated entity for this test.
         *
         * This is a static method, as tests for other entities might also need it,
         * if they test an entity which requires the current entity.
         */
        @JvmStatic
        fun createUpdatedEntity(em: EntityManager): Category {
            val category = Category(

                description = UPDATED_DESCRIPTION,

                sortOrder = UPDATED_SORT_ORDER,

                dateAdded = UPDATED_DATE_ADDED,

                dateModified = UPDATED_DATE_MODIFIED,

                status = UPDATED_STATUS

            )

            return category
        }
    }
}
