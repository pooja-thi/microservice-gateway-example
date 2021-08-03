
package com.library.gateway.config

import io.r2dbc.spi.ConnectionFactory
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.convert.converter.Converter
import org.springframework.core.env.Environment
import org.springframework.data.convert.CustomConversions
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.data.r2dbc.dialect.R2dbcDialect
import org.springframework.data.r2dbc.query.UpdateMapper
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import org.springframework.data.relational.core.dialect.RenderContextFactory
import org.springframework.data.relational.core.sql.render.SqlRenderer
import org.springframework.transaction.annotation.EnableTransactionManagement
import tech.jhipster.config.JHipsterConstants
import tech.jhipster.config.h2.H2ConfigurationHelper
import java.sql.SQLException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.BitSet

@Configuration
@EnableR2dbcRepositories("com.library.gateway.repository")
@EnableTransactionManagement
public class DatabaseConfiguration(val env: Environment) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Open the TCP port for the H2 database, so it is available remotely.
     *
     * @return the H2 database TCP server.
     * @throws SQLException if the server failed to start.
     */
    @Throws(SQLException::class)
    @Bean(initMethod = "start", destroyMethod = "stop")
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    fun h2TCPServer(): Any {
        val port = getValidPortForH2()
        log.debug("H2 database is available on port $port")
        return H2ConfigurationHelper.createServer(port)
    }

    private fun getValidPortForH2(): String {
        var port = Integer.parseInt(env.getProperty("server.port"))
        if (port < 10000) {
            port += 10000
        } else {
            if (port < 63536) {
                port += 2000
            } else {
                port -= 2000
            }
        }
        return port.toString()
    }
    // LocalDateTime seems to be the only type that is supported across all drivers atm
    // See https://github.com/r2dbc/r2dbc-h2/pull/139 https://github.com/mirromutth/r2dbc-mysql/issues/105
    @Bean
    fun r2dbcCustomConversions(connectionFactory: ConnectionFactory): R2dbcCustomConversions {
        val dialect = DialectResolver.getDialect(connectionFactory)
        val converters = dialect.converters.toMutableList()
        converters.add(InstantWriteConverter)
        converters.add(InstantReadConverter)
        converters.add(BitSetReadConverter)
        converters.add(DurationWriteConverter)
        converters.add(DurationReadConverter)
        converters.add(ZonedDateTimeReadConverter)
        converters.add(ZonedDateTimeWriteConverter)
        converters.addAll(R2dbcCustomConversions.STORE_CONVERTERS)
        return R2dbcCustomConversions(CustomConversions.StoreConversions.of(dialect.simpleTypeHolder, converters), mutableListOf<Any>())
    }

    @Bean
    fun dialect(connectionFactory: ConnectionFactory) = DialectResolver.getDialect(connectionFactory)

    @Bean
    fun updateMapper(dialect: R2dbcDialect, mappingR2dbcConverter: MappingR2dbcConverter) = UpdateMapper(dialect, mappingR2dbcConverter)

    @Bean
    fun sqlRenderer(dialect: R2dbcDialect): SqlRenderer {
        val factory = RenderContextFactory(dialect)
        return SqlRenderer.create(factory.createRenderContext())
    }

    @WritingConverter
    object InstantWriteConverter : Converter<Instant, LocalDateTime> {
        override fun convert(source: Instant) = LocalDateTime.ofInstant(source, ZoneOffset.UTC)
    }

    @ReadingConverter
    object InstantReadConverter : Converter<LocalDateTime, Instant> {
        override fun convert(localDateTime: LocalDateTime) = localDateTime.toInstant(ZoneOffset.UTC)
    }

    @ReadingConverter
    object BitSetReadConverter : Converter<BitSet, Boolean> {
        override fun convert(bitSet: BitSet): Boolean = bitSet.get(0)
    }

    @ReadingConverter
    object ZonedDateTimeReadConverter : Converter<LocalDateTime, ZonedDateTime> {
        override fun convert(localDateTime: LocalDateTime) = ZonedDateTime.of(localDateTime, ZoneOffset.UTC)
    }

    @WritingConverter
    object ZonedDateTimeWriteConverter : Converter<ZonedDateTime, LocalDateTime> {
        override fun convert(zonedDateTime: ZonedDateTime) = zonedDateTime.toLocalDateTime()
    }

    @WritingConverter
    object DurationWriteConverter : Converter<Duration, Long> {
        override fun convert(source: Duration?) = if (source != null) source.toMillis() else null
    }

    @ReadingConverter
    object DurationReadConverter : Converter<Long, Duration> {
        override fun convert(source: Long?) = if (source != null) Duration.ofMillis(source) else null
    }
}
