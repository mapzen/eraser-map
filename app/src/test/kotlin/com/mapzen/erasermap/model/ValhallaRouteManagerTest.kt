package com.mapzen.erasermap.model

import android.content.Context
import android.content.res.Resources
import android.test.mock.MockContext
import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

public class ValhallaRouteManagerTest {
    val routerFactory = TestRouterFactory()
    var routeManager: ValhallaRouteManager? = null
    var router: TestRouter? = null

    @Before fun setup() {
        val context = Mockito.mock(Context::class.java);
        val resources = Mockito.mock(Resources::class.java);
        Mockito.`when`(context.resources).thenReturn(resources);
        Mockito.`when`(context.packageName).thenReturn("test");
        Mockito.`when`(resources.getIdentifier("turn_by_turn_key", "string",
            "test")).thenReturn(1)
        Mockito.`when`(resources.getString(1)).thenThrow(
            Resources.NotFoundException::class.java)
        routeManager = ValhallaRouteManager(TestAppSettings(), routerFactory, context)
        router = routerFactory.getRouter(context) as TestRouter
    }

    @Test fun shouldNotBeNull() {
        assertThat(routeManager).isNotNull()
    }

    @Test fun fetchRoute_shouldIncludeNameInRouteRequest() {
        val feature = TestHelper.getTestFeature()
        routeManager?.origin = TestHelper.getTestLocation()
        routeManager?.destination = feature
        routeManager?.fetchRoute(TestRouteCallback())
        assertThat(router?.name).isEqualTo("Name")
    }

    @Test fun fetchRoute_shouldNotIncludeNameInRouteRequestIfFeatureIsAnAddress() {
        val feature = TestHelper.getTestFeature()
        feature.properties.layer = "address"
        routeManager?.origin = TestHelper.getTestLocation()
        routeManager?.destination = feature
        routeManager?.fetchRoute(TestRouteCallback())
        assertThat(router?.name).isNull()
    }

    class TestRouteCallback : RouteCallback {
        override fun failure(statusCode: Int) {
        }

        override fun success(route: Route) {
        }
    }
}
