package com.library.user.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import java.io.Serializable
import javax.persistence.*

/**
 * A Customer.
 */
@Entity
@Table(name = "customer")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
data class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    var id: Long? = null,
    @Column(name = "first_name")
    var firstName: String? = null,

    @Column(name = "last_name")
    var lastName: String? = null,

    @Column(name = "email")
    var email: String? = null,

    @Column(name = "telephone")
    var telephone: String? = null,

    @OneToMany(mappedBy = "customer")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)

    @JsonIgnoreProperties(
        value = [
            "customer"
        ],
        allowSetters = true
    )
    var addresses: MutableSet<Address>? = mutableSetOf(),

    // jhipster-needle-entity-add-field - JHipster will add fields here
) : Serializable {

    fun addAddress(address: Address): Customer {
        if (this.addresses == null) {
            this.addresses = mutableSetOf()
        }
        this.addresses?.add(address)
        address.customer = this
        return this
    }

    fun removeAddress(address: Address): Customer {
        this.addresses?.remove(address)
        address.customer = null
        return this
    }
    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Customer) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "Customer{" +
        "id=$id" +
        ", firstName='$firstName'" +
        ", lastName='$lastName'" +
        ", email='$email'" +
        ", telephone='$telephone'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
