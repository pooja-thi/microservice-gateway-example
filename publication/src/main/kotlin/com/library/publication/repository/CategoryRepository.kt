package com.library.publication.repository

import com.library.publication.domain.Category
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

/**
 * Spring Data SQL repository for the [Category] entity.
 */
@Repository
interface CategoryRepository : JpaRepository<Category, Long> {

    @Query(
        value = "select distinct category from Category category left join fetch category.books",
        countQuery = "select count(distinct category) from Category category"
    )
    fun findAllWithEagerRelationships(pageable: Pageable): Page<Category>

    @Query("select distinct category from Category category left join fetch category.books")
    fun findAllWithEagerRelationships(): MutableList<Category>

    @Query("select category from Category category left join fetch category.books where category.id =:id")
    fun findOneWithEagerRelationships(@Param("id") id: Long): Optional<Category>
}
