package com.mapzen.erasermap.model

import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

public class ValhallaRouteManagerTest {
    val routerFactory = TestRouterFactory()
    val routeManager = ValhallaRouteManager(TestAppSettings(), routerFactory)

    @Test fun shouldNotBeNull() {
        assertThat(routeManager).isNotNull()
    }

    @Test fun fetchRoute_shouldIncludeNameInRouteRequest() {
        val feature = TestHelper.getTestFeature()
        routeManager.origin = TestHelper.getTestLocation()
        routeManager.destination = feature
        routeManager.fetchRoute(TestRouteCallback())
        assertThat(routerFactory.router.name).isEqualTo("Name")
    }

    @Test fun fetchRoute_shouldNotIncludeNameInRouteRequestIfFeatureIsAnAddress() {
        val feature = TestHelper.getTestFeature()
        feature.properties.layer = "address"
        routeManager.origin = TestHelper.getTestLocation()
        routeManager.destination = feature
        routeManager.fetchRoute(TestRouteCallback())
        assertThat(routerFactory.router.name).isNull()
    }

    class TestRouteCallback : RouteCallback {
        override fun failure(statusCode: Int) {
        }

        override fun success(route: Route) {
        }
    }
}
