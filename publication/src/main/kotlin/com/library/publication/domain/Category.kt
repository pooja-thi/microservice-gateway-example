package com.library.publication.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.library.publication.domain.enumeration.CategoryStatus
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A Category.
 */
@Entity
@Table(name = "category")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,
    @get: NotNull
    @Column(name = "description", nullable = false)
    var description: String? = null,

    @Column(name = "sort_order")
    var sortOrder: Int? = null,

    @Column(name = "date_added")
    var dateAdded: LocalDate? = null,

    @Column(name = "date_modified")
    var dateModified: LocalDate? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    var status: CategoryStatus? = null,

    @ManyToOne
    @JsonIgnoreProperties(
        value = [
            "parent", "books"
        ],
        allowSetters = true
    )
    var parent: Category? = null,

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(
        name = "rel_category__book",
        joinColumns = [
            JoinColumn(name = "category_id")
        ],
        inverseJoinColumns = [
            JoinColumn(name = "book_id")
        ]
    )
    @JsonIgnoreProperties(
        value = [
            "categories"
        ],
        allowSetters = true
    )
    var books: MutableSet<Book>? = mutableSetOf(),

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    fun addBook(book: Book): Category {
        if (this.books == null) {
            this.books = mutableSetOf()
        }
        this.books?.add(book)
        book.categories?.add(this)
        return this
    }

    fun removeBook(book: Book): Category {
        this.books?.remove(book)
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Category) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Category{" +
        "id=$id" +
        ", description='$description'" +
        ", sortOrder=$sortOrder" +
        ", dateAdded='$dateAdded'" +
        ", dateModified='$dateModified'" +
        ", status='$status'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
