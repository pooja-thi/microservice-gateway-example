package com.library.gateway.repository

import com.library.gateway.domain.Authority
import com.library.gateway.domain.User
import org.apache.commons.beanutils.BeanComparator
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.convert.R2dbcConverter
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.r2dbc.repository.R2dbcRepository
import org.springframework.data.relational.core.sql.Column
import org.springframework.data.relational.core.sql.Expression
import org.springframework.data.relational.core.sql.Table
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.util.function.Tuple2
import reactor.util.function.Tuples
import java.util.Optional

/**
 * Spring Data R2DBC repository for the {@link User} entity.
 */
@Repository
interface UserRepository : R2dbcRepository<User, String>, UserRepositoryInternal {

    fun findOneByLogin(login: String): Mono<User>

    fun findAllByIdNotNull(pageable: Pageable): Flux<User>

    fun findAllByIdNotNullAndActivatedIsTrue(pageable: Pageable): Flux<User>

    override fun count(): Mono<Long>

    @Query("INSERT INTO jhi_user_authority VALUES(:userId, :authority)")
    fun saveUserAuthority(userId: String, authority: String): Mono<Void>

    @Query("DELETE FROM jhi_user_authority")
    fun deleteAllUserAuthorities(): Mono<Void>

    @Query("DELETE FROM jhi_user_authority WHERE user_id = :userId")
    fun deleteUserAuthorities(userId: Long): Mono<Void>
}

interface UserRepositoryInternal {

    fun findOneWithAuthoritiesByLogin(login: String): Mono<User>

    fun create(user: User): Mono<User>
    fun findAllWithAuthorities(pageable: Pageable): Flux<User>
}

class UserRepositoryInternalImpl(val db: DatabaseClient, val r2dbcEntityTemplate: R2dbcEntityTemplate, val r2dbcConverter: R2dbcConverter) : UserRepositoryInternal {

    override fun findOneWithAuthoritiesByLogin(login: String): Mono<User> {
        return findOneWithAuthoritiesBy("login", login)
    }

    override fun findAllWithAuthorities(pageable: Pageable): Flux<User> {
        val property = pageable.sort.map(Sort.Order::getProperty).first()
        val direction = pageable.sort.map(Sort.Order::getDirection).first()
        val comparator = if (direction == Sort.DEFAULT_DIRECTION) { BeanComparator(property) } else { BeanComparator<Any>(property).reversed() }
        val page = pageable.pageNumber
        val size = pageable.pageSize

        return db
            .sql("SELECT * FROM jhi_user u LEFT JOIN jhi_user_authority ua ON u.id=ua.user_id")
            .map { row, metadata ->
                return@map Tuples.of(
                    r2dbcConverter.read(User::class.java, row, metadata),
                    Optional.ofNullable(row.get("authority_name", String::class.java))
                )
            }.all()
            .groupBy { it.t1.login }
            .flatMap { it.collectList().map { t -> updateUserWithAuthorities(t[0].t1, t) } }
            .sort(comparator)
            .skip((page * size).toLong())
            .take(size.toLong())
    }

    override fun create(user: User): Mono<User> {
        return r2dbcEntityTemplate.insert(User::class.java).using(user)
            .defaultIfEmpty(user)
    }

    private fun findOneWithAuthoritiesBy(fieldName: String, fieldValue: Any): Mono<User> {
        return db.sql("SELECT * FROM jhi_user u LEFT JOIN jhi_user_authority ua ON u.id=ua.user_id WHERE u.$fieldName = :$fieldName")
            .bind(fieldName, fieldValue)
            .map { row, metadata ->
                return@map Tuples.of(
                    r2dbcConverter.read(User::class.java, row, metadata),
                    Optional.ofNullable(row.get("authority_name", String::class.java))
                )
            }.all()
            .collectList()
            .filter { it.isNotEmpty() }
            .map { l -> updateUserWithAuthorities(l[0].t1, l) }
    }

    private fun updateUserWithAuthorities(user: User, tuples: List<Tuple2<User, Optional<String>>>): User {
        user.authorities = tuples.filter { it.t2.isPresent }
            .map {
                val authority = Authority()
                authority.name = it.t2.get()
                authority
            }.toMutableSet()
        return user
    }
}

class UserSqlHelper {
    fun getColumns(table: Table, columnPrefix: String): MutableList<Expression> {
        val columns = mutableListOf<Expression>()
        columns.add(Column.aliased("id", table, columnPrefix + "_id"))
        columns.add(Column.aliased("login", table, columnPrefix + "_login"))
        columns.add(Column.aliased("first_name", table, columnPrefix + "_first_name"))
        columns.add(Column.aliased("last_name", table, columnPrefix + "_last_name"))
        columns.add(Column.aliased("email", table, columnPrefix + "_email"))
        columns.add(Column.aliased("activated", table, columnPrefix + "_activated"))
        columns.add(Column.aliased("lang_key", table, columnPrefix + "_lang_key"))
        columns.add(Column.aliased("image_url", table, columnPrefix + "_image_url"))
        return columns
    }
}
