package com.library.gateway.web.filter

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class SpaWebFilter : WebFilter {
    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        val path = exchange.request.uri.path
        if (!path.startsWith("/api") && !path.startsWith("/management") && !path.startsWith("/login") &&
            !path.startsWith("/services") && !path.startsWith("/swagger") && !path.startsWith("/v2/api-docs") && !path.startsWith("/v3/api-docs") &&
            path.matches(Regex("[^\\\\.]*"))
        ) {
            return chain.filter(exchange.mutate().request(exchange.request.mutate().path("/index.html").build()).build())
        }
        return chain.filter(exchange)
    }
}
