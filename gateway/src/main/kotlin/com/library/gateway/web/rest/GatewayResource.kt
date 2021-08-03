package com.library.gateway.web.rest

import com.library.gateway.security.ADMIN
import com.library.gateway.web.rest.vm.RouteVM
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.client.discovery.DiscoveryClient
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.http.ResponseEntity
import org.springframework.security.access.annotation.Secured
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing Gateway configuration.
 */
@RestController
@RequestMapping("/api/gateway")
class GatewayResource(private val routeLocator: RouteLocator, private val discoveryClient: DiscoveryClient, @Value("\${spring.application.name}") val appName: String) {

    /**
     * `GET  /routes` : get the active routes.
     *
     * @return the [ResponseEntity] with status `200 (OK)` and with body the list of routes.
     */
    @GetMapping("/routes")
    @Secured(ADMIN)
    fun activeRoutes(): ResponseEntity<List<RouteVM>> {
        val routes = routeLocator.routes
        val routeVMs = mutableListOf<RouteVM>()
        routes.subscribe { route ->
            val routeVM = RouteVM()
            // Manipulate strings to make Gateway routes look like Zuul's
            val predicate = route.predicate.toString()
            val path = predicate.substring(predicate.indexOf("[") + 1, predicate.indexOf("]"))
            routeVM.path = path
            val serviceId = route.id.substring(route.id.indexOf("_") + 1).toLowerCase()
            routeVM.serviceId = serviceId
            // Exclude gateway app from routes
            if (serviceId.toLowerCase() != appName.toLowerCase()) {
                routeVM.serviceInstances = discoveryClient.getInstances(serviceId)
                routeVMs.add(routeVM)
            }
        }
        return ResponseEntity.ok(routeVMs)
    }
}
