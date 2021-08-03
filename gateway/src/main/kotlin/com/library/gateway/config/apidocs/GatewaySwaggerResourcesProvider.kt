package com.library.gateway.config.apidocs

import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import reactor.core.scheduler.Schedulers
import springfox.documentation.swagger.web.SwaggerResource
import springfox.documentation.swagger.web.SwaggerResourcesProvider
import tech.jhipster.config.JHipsterConstants
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Retrieves all registered microservices Swagger resources.
 */
@Component
@Primary
@Profile(JHipsterConstants.SPRING_PROFILE_API_DOCS)
@Configuration
class GatewaySwaggerResourcesProvider(
    private val routeLocator: RouteLocator
) : SwaggerResourcesProvider {

    @Value("\${eureka.instance.appname:gateway}")
    private var gatewayName: String? = null

    override fun get(): MutableList<SwaggerResource> {
        val swaggerResources = mutableListOf(
            // Add the default swagger resource that correspond to the gateway's own swagger doc
            swaggerResource(gatewayName.plus(" (default)"), "/v3/api-docs"),
            swaggerResource(gatewayName.plus(" (management)"), "/v3/api-docs?group=management")
        )

        val microservices = routeLocator.routes.map { getMicroservicesName(it) }
            .collectList()
            .defaultIfEmpty(listOf())
            .subscribeOn(Schedulers.boundedElastic())
            .toFuture()
            .orTimeout(10, TimeUnit.SECONDS)
            .join()

        microservices
            .filter { isNotGateway(it) }
            .forEach { swaggerResources.add(swaggerResource(it, getMicroserviceApiDocs(it))) }

        return swaggerResources
    }

    companion object {
        fun swaggerResource(name: String, location: String) =
            SwaggerResource().apply {
                this.name = name
                this.location = location
                swaggerVersion = "3.0"
            }
    }

    private fun isNotGateway(name: String) = !name.equals(gatewayName, ignoreCase = true)

    private fun getMicroserviceApiDocs(name: String): String {
        return "/services/$name/v3/api-docs"
    }

    private fun getMicroservicesName(route: Route): String {
        return route.uri.toString().replace("lb://", "").toLowerCase()
    }
}
