package com.mapzen.erasermap.presenter

import android.location.Location
import android.view.MotionEvent
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

    @Test fun onRouteStart_shouldResetRouteEngine() {
        val route1 = Route(getFixture("valhalla_route"))
        val route2 = Route(getFixture("valhalla_route"))
        routeEngine.route = route1
        routePresenter.onRouteStart(route2)
        assertThat(routeEngine.route).isEqualTo(route2)
    }

    @Test fun onRouteResume_shouldNotResetRouteEngine() {
        val route1 = Route(getFixture("valhalla_route"))
        val route2 = Route(getFixture("valhalla_route"))
        routeEngine.route = route1
        routePresenter.onRouteResume(route2)
        assertThat(routeEngine.route).isEqualTo(route1)
    }

    @Test fun onMapGesture_shouldShowResumeButton() {
        routeController.isResumeButtonVisible = false
        routePresenter.onMapGesture(MotionEvent.ACTION_MOVE, 1, RoutePresenter.GESTURE_MIN_DELTA,
                RoutePresenter.GESTURE_MIN_DELTA)
        assertThat(routeController.isResumeButtonVisible).isTrue()
    }

    @Test fun onMapGesture_shouldDisableLocationTracking() {
        routePresenter.onResumeButtonClick()
        routePresenter.onMapGesture(MotionEvent.ACTION_MOVE, 1, RoutePresenter.GESTURE_MIN_DELTA,
                RoutePresenter.GESTURE_MIN_DELTA)
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNull()
    }

    @Test fun onMapGesture_shouldNotDisableLocationTrackingOnActionDown() {
        routePresenter.onResumeButtonClick()
        routePresenter.onMapGesture(MotionEvent.ACTION_DOWN, 1, RoutePresenter.GESTURE_MIN_DELTA,
                RoutePresenter.GESTURE_MIN_DELTA)
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNotNull()
    }

    @Test fun onMapGesture_shouldNotDisableLocationTrackingIfMoreThanOnePointer() {
        routePresenter.onResumeButtonClick()
        routePresenter.onMapGesture(MotionEvent.ACTION_MOVE, 2, RoutePresenter.GESTURE_MIN_DELTA,
                RoutePresenter.GESTURE_MIN_DELTA)
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNotNull()
    }

    @Test fun onMapGesture_shouldNotDisableLocationTrackingIfDeltaIsLessThanMinDistance() {
        routePresenter.onResumeButtonClick()
        routePresenter.onMapGesture(MotionEvent.ACTION_MOVE, 1, 0f, 0f)
        routePresenter.onUpdateSnapLocation(Location("test"))
        assertThat(routeController.mapLocation).isNotNull()
    }

    @Test fun onResumeButtonClick_shouldHideResumeButton() {
        routeController.isResumeButtonVisible = true
        routePresenter.onResumeButtonClick()
        assertThat(routeController.isResumeButtonVisible).isFalse()
    }

    @Test fun onResumeButtonClick_shouldEnableLocationTracking() {
        routePresenter.onMapGesture(MotionEvent.ACTION_MOVE, 1, RoutePresenter.GESTURE_MIN_DELTA,
                RoutePresenter.GESTURE_MIN_DELTA)
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

    @Test fun onClearRoute_shouldHideRouteIcon() {
        routeController.isRouteIconVisible = true
        routePresenter.onRouteClear()
        assertThat(routeController.isRouteIconVisible).isFalse()
    }

    @Test fun onClearRoute_shouldHideRouteLine() {
        routeController.isRouteLineVisible = true
        routePresenter.onRouteClear()
        assertThat(routeController.isRouteLineVisible).isFalse()
    }
}
