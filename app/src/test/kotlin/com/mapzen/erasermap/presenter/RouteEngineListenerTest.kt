package com.mapzen.erasermap.presenter

import com.mapzen.erasermap.view.TestRouteController
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class RouteEngineListenerTest {
    val routeListener = RouteEngineListener()
    val routeController = TestRouteController()

    @Before fun setUp() {
        routeListener.controller = routeController
    }

    @Test fun shouldNotBeNull() {
        assertThat(routeListener).isNotNull()
    }
}
