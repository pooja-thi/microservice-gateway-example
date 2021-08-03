package com.library.gateway.domain

import com.fasterxml.jackson.annotation.JsonIgnore
import com.library.gateway.config.LOGIN_REGEX
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor
import org.springframework.data.annotation.Transient
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.io.Serializable
import java.time.Instant
import javax.validation.constraints.Email
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

/**
 * A user.
 */
@Table("jhi_user")
class User(

    @Id
    var id: String? = null,

    @field:NotNull
    @field:Pattern(regexp = LOGIN_REGEX)
    @field:Size(min = 1, max = 50)
    var login: String? = null,

    @field:Size(max = 50)
    @Column("first_name")
    var firstName: String? = null,

    @field:Size(max = 50)
    @Column("last_name")
    var lastName: String? = null,

    @field:Email
    @field:Size(min = 5, max = 254)
    var email: String? = null,

    @field:NotNull
    var activated: Boolean? = false,

    @field:Size(min = 2, max = 10)
    @Column("lang_key")
    var langKey: String? = null,

    @field:Size(max = 256)
    @Column("image_url")
    var imageUrl: String? = null,

    @JsonIgnore

    @Transient

    var authorities: MutableSet<Authority> = mutableSetOf(),
    createdBy: String? = null,
    createdDate: Instant? = Instant.now(),
    lastModifiedBy: String? = null,
    lastModifiedDate: Instant? = Instant.now()
) : AbstractAuditingEntity(createdBy, createdDate, lastModifiedBy, lastModifiedDate), Serializable {

    @PersistenceConstructor
    constructor(
        id: String?,
        login: String?,
        firstName: String?,
        lastName: String?,
        email: String?,
        activated: Boolean?,
        langKey: String?,
        imageUrl: String?,
        createdBy: String?,
        createdDate: Instant?,
        lastModifiedBy: String?,
        lastModifiedDate: Instant?
    ) : this (
        id,
        login,
        firstName,
        lastName,
        email,
        activated,
        langKey,
        imageUrl,
        mutableSetOf(),
        createdBy,
        createdDate,
        lastModifiedBy,
        lastModifiedDate,
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false
        if (other.id == null || id == null) return false

        return id == other.id
    }

    override fun hashCode() = 31

    override fun toString() =
        "User{" +
            "login='" + login + '\'' +
            ", firstName='" + firstName + '\'' +
            ", lastName='" + lastName + '\'' +
            ", email='" + email + '\'' +
            ", imageUrl='" + imageUrl + '\'' +
            ", activated='" + activated + '\'' +
            ", langKey='" + langKey + '\'' +
            "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}
