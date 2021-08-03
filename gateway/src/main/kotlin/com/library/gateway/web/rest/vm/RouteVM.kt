package com.library.gateway.web.rest.vm

import org.springframework.cloud.client.ServiceInstance

/**
 * View Model that stores a route managed by the Gateway.
 */
class RouteVM(
    var path: String? = null,
    var serviceId: String? = null,
    var serviceInstances: List<ServiceInstance>? = null
)
