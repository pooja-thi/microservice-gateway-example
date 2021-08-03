package com.library.user.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import javax.persistence.*
import javax.validation.constraints.*

/**
 * A Address.
 */
@Entity
@Table(name = "address")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,
    @Column(name = "address_1")
    var address1: String? = null,

    @Column(name = "address_2")
    var address2: String? = null,

    @Column(name = "city")
    var city: String? = null,

    @get: NotNull
    @get: Size(max = 10)
    @Column(name = "postcode", length = 10, nullable = false)
    var postcode: String? = null,

    @get: NotNull
    @get: Size(max = 2)
    @Column(name = "country", length = 2, nullable = false)
    var country: String? = null,

    @ManyToOne
    @JsonIgnoreProperties(
        value = [
            "addresses"
        ],
        allowSetters = true
    )
    var customer: Customer? = null,

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Address) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Address{" +
        "id=$id" +
        ", address1='$address1'" +
        ", address2='$address2'" +
        ", city='$city'" +
        ", postcode='$postcode'" +
        ", country='$country'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
