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

    @Test fun onInstructionComplete_shouldPlayPostInstruction() {
        routeListener.onInstructionComplete(1)
        assertThat(routeController.post).isEqualTo(1)
    }

    @Test fun onInstructionComplete_shouldNotPlayPostInstructionWhenIndexIsZero() {
        routeListener.onInstructionComplete(0)
        assertThat(routeController.post).isEqualTo(-1)
    }
}
