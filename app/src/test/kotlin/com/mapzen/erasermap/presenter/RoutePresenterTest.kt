package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.dummy.TestHelper
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
        routePresenter onRouteStart route2
        assertThat(routeEngine.route).isEqualTo(route1)
    }

    @Test fun onMapGesture_shouldShowResumeButton() {
        routeController.isResumeButtonVisible = false
        routePresenter.onMapGesture()
        assertThat(routeController.isResumeButtonVisible).isTrue()
    }

    @Test fun onMapGesture_shouldDisableLocationTracking() {
        routePresenter.onResumeButtonClick()
        routePresenter.onMapGesture()
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNull()
    }

    @Test fun onResumeButtonClick_shouldHideResumeButton() {
        routeController.isResumeButtonVisible = true
        routePresenter.onResumeButtonClick()
        assertThat(routeController.isResumeButtonVisible).isFalse()
    }

    @Test fun onResumeButtonClick_shouldEnableLocationTracking() {
        routePresenter.onMapGesture()
        routePresenter.onResumeButtonClick()
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNotNull()
    }

    @Test fun onInstructionPagerTouch_shouldShowResumeButton() {
        routeController.isResumeButtonVisible = false
        routePresenter.onInstructionPagerTouch()
        assertThat(routeController.isResumeButtonVisible).isTrue()
    }

    @Test fun onInstructionPagerTouch_shouldDisableLocationTracking() {
        routePresenter.onResumeButtonClick()
        routePresenter.onInstructionPagerTouch()
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNull()
    }

    @Test fun onInstructionSelected_shouldCenterMapOnLocation() {
        val instruction = TestHelper.getTestInstruction()
        val location = TestHelper.getTestLocation()
        instruction.location = location
        routePresenter.onInstructionPagerTouch()
        routePresenter.onInstructionSelected(instruction)
        assertThat(routeController.mapLocation).isEqualTo(location)
    }
}
