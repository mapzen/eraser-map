package com.mapzen.erasermap.presenter

import com.mapzen.erasermap.dummy.TestHelper.getFixture
import com.mapzen.helpers.RouteEngine
import com.mapzen.valhalla.Route
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

public class RoutePresenterTest {
    private val routeEngine = RouteEngine()
    private val routePresenter = RoutePresenterImpl(routeEngine)

    @Test
    public fun shouldNotBeNull() {
        assertThat(routePresenter).isNotNull()
    }

    @Test
    public fun setRoute_shouldNotResetRouteEngineWhileRouting() {
        val route1 = Route(getFixture("valhalla_route"))
        val route2 = Route(getFixture("valhalla_route"))
        routeEngine.setRoute(route1)
        routePresenter.setRoute(route2)
        assertThat(routeEngine.getRoute()).isEqualTo(route1)
    }
}
