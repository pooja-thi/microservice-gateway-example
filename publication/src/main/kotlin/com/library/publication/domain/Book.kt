package com.library.publication.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.hibernate.annotations.Type
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A Book.
 */
@Entity
@Table(name = "book")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Book(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,
    @get: NotNull
    @Column(name = "title", nullable = false)
    var title: String? = null,

    @Column(name = "author")
    var author: String? = null,

    @Column(name = "keywords")
    var keywords: String? = null,

    @Lob
    @Type(type = "org.hibernate.type.TextType")
    @Column(name = "description")
    var description: String? = null,

    @Column(name = "rating")
    var rating: Int? = null,

    @Column(name = "date_added")
    var dateAdded: LocalDate? = null,

    @Column(name = "date_modified")
    var dateModified: LocalDate? = null,

    @Lob
    @Column(name = "image")
    var image: ByteArray? = null,

    @Column(name = "image_content_type")
    var imageContentType: String? = null,

    @ManyToMany(mappedBy = "books")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

    @JsonIgnoreProperties(
        value = [
            "parent", "books"
        ],
        allowSetters = true
    )
    var categories: MutableSet<Category>? = mutableSetOf(),

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    fun addCategory(category: Category): Book {
        if (this.categories == null) {
            this.categories = mutableSetOf()
        }
        this.categories?.add(category)
        category.books?.add(this)
        return this
    }

    fun removeCategory(category: Category): Book {
        this.categories?.remove(category)
        category.books?.remove(this)
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Book) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Book{" +
        "id=$id" +
        ", title='$title'" +
        ", author='$author'" +
        ", keywords='$keywords'" +
        ", description='$description'" +
        ", rating=$rating" +
        ", dateAdded='$dateAdded'" +
        ", dateModified='$dateModified'" +
        ", image='$image'" +
        ", imageContentType='$imageContentType'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
