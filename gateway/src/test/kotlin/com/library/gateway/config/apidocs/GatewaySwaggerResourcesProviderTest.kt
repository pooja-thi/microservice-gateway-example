package com.library.gateway.config.apidocs

import com.library.gateway.config.apidocs.GatewaySwaggerResourcesProvider.Companion.swaggerResource
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.springframework.cloud.gateway.route.Route
import org.springframework.cloud.gateway.route.RouteLocator
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.util.ReflectionTestUtils
import reactor.core.publisher.Flux
import springfox.documentation.swagger.web.SwaggerResourcesProvider
import java.net.URI
import java.util.List

@ExtendWith(SpringExtension::class)
class GatewaySwaggerResourcesProviderTest {

    @InjectMocks
    private lateinit var gatewaySwaggerResourcesProvider: GatewaySwaggerResourcesProvider

    @Mock
    private lateinit var routeLocator: RouteLocator

    @Mock
    private lateinit var swaggerResourcesProvider: SwaggerResourcesProvider

    @Test
    fun shouldGet() {
        // Given
        ReflectionTestUtils.setField(gatewaySwaggerResourcesProvider, "gatewayName", "burger")
        `when`(swaggerResourcesProvider.get())
            .thenReturn(
                List.of(
                    swaggerResource("default", "/v3/api-docs"),
                    swaggerResource("default", "/v3/api-docs?group=management"),
                    swaggerResource("default", "/v3/api-docs?group=openapi")
                )
            )
        `when`(routeLocator.getRoutes())
            .thenReturn(
                Flux.just(
                    Route
                        .async()
                        .id("ReactiveCompositeDiscoveryClient_BURGER")
                        .uri(URI.create("lb://BURGER"))
                        .predicate { true }
                        .build(),
                    Route
                        .async()
                        .id("ReactiveCompositeDiscoveryClient_BEER")
                        .uri(URI.create("lb://BEER"))
                        .predicate { true }
                        .build()
                )
            )

        // When
        val result = gatewaySwaggerResourcesProvider.get()

        // Then
        assertThat(result).isNotEmpty()
        assertThat(result.size).isEqualTo(3)

        assertThat(result[0].name).isEqualTo("burger (default)")
        assertThat(result[0].url).isEqualTo("/v3/api-docs")

        assertThat(result[1].name).isEqualTo("burger (management)")
        assertThat(result[1].url).isEqualTo("/v3/api-docs?group=management")

        assertThat(result[2].name).isEqualTo("beer")
        assertThat(result[2].url).isEqualTo("/services/beer/v3/api-docs")
    }
}
