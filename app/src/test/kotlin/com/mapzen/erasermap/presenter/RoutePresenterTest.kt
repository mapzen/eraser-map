package com.mapzen.erasermap.presenter

import com.mapzen.erasermap.dummy.TestHelper.getFixture
import com.mapzen.erasermap.view.TestRouteController
import com.mapzen.helpers.RouteEngine
import com.mapzen.valhalla.Route
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

public class RoutePresenterTest {
    val routeEngine = RouteEngine()
    val routeListener = RouteEngineListener()
    val routePresenter = RoutePresenterImpl(routeEngine, routeListener)
    val routeController = TestRouteController()

    @Before fun setUp() {
        routePresenter.routeController = routeController
    }

    @Test fun shouldNotBeNull() {
        assertThat(routePresenter).isNotNull()
    }

    @Test fun setRoute_shouldNotResetRouteEngineWhileRouting() {
        val route1 = Route(getFixture("valhalla_route"))
        val route2 = Route(getFixture("valhalla_route"))
        routeEngine.route = route1
        routePresenter setRoute route2
        assertThat(routeEngine.route).isEqualTo(route1)
    }

    @Test fun onMapGesture_shouldShowResumeButton() {
        routeController.isResumeButtonVisible = false
        routePresenter.onMapGesture()
        assertThat(routeController.isResumeButtonVisible).isTrue()
    }

    @Test fun onResumeButtonClick_shouldShowResumeButton() {
        routeController.isResumeButtonVisible = true
        routePresenter.onResumeButtonClick()
        assertThat(routeController.isResumeButtonVisible).isFalse()
    }
}
