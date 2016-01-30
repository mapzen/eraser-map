package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.model.event.RouteCancelEvent
import com.mapzen.erasermap.view.MapListToggleButton
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.helpers.RouteEngine
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus

public class RoutePresenterImpl(private val routeEngine: RouteEngine,
        private val routeEngineListener: RouteEngineListener,
        private val bus: Bus) : RoutePresenter {

    override var routeController: RouteViewController? = null
        set(value) {
            field = value
            routeEngineListener.controller = value
        }

    override var currentInstructionIndex: Int = 0

    private var route: Route? = null
    private var isTrackingCurrentLocation: Boolean = true

    override fun onLocationChanged(location: Location) {
        routeEngine.onLocationChanged(location)
    }

    override fun onRouteStart(route: Route?) {
        this.route = route
        routeEngine.setListener(routeEngineListener)
        currentInstructionIndex = 0
        routeController?.setCurrentInstruction(0)
        routeEngine.route = route
    }

    override fun onRouteResume(route: Route?) {
        routeController?.setCurrentInstruction(currentInstructionIndex)
    }

    override fun onMapPan(deltaX: Float, deltaY: Float) {
        if (Math.abs(deltaX) >= RoutePresenter.GESTURE_MIN_DELTA ||
            Math.abs(deltaY) >= RoutePresenter.GESTURE_MIN_DELTA) {
            isTrackingCurrentLocation = false
            routeController?.showResumeButton()
        }
    }

    override fun onResumeButtonClick() {
        isTrackingCurrentLocation = true
        routeController?.hideResumeButton()
        routeController?.centerMapOnCurrentLocation()
    }

    override fun onInstructionPagerTouch() {
        isTrackingCurrentLocation = false
        routeController?.showResumeButton()
    }

    override fun onInstructionSelected(instruction: Instruction) {
        if (!isTrackingCurrentLocation) {
            routeController?.centerMapOnLocation(instruction.location)
        }
    }

    override fun onUpdateSnapLocation(location: Location) {
        routeController?.showRouteIcon(location)
        if (isTrackingCurrentLocation) {
            routeController?.centerMapOnLocation(location)
        }
    }

    override fun onRouteClear() {
        routeController?.hideRouteIcon()
        routeController?.hideRouteLine()
    }

    override fun onMapListToggleClick(state: MapListToggleButton.MapListState) {
        if (state == MapListToggleButton.MapListState.LIST) {
            routeController?.showRouteDirectionList()
        } else {
            routeController?.hideRouteDirectionList()
        }
    }

    override fun onRouteCancelButtonClick() {
        bus.post(RouteCancelEvent())
    }
}
