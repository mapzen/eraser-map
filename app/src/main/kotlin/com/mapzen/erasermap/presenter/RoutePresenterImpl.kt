package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.model.event.RouteCancelEvent
import com.mapzen.erasermap.view.MapListToggleButton
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.helpers.RouteEngine
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
import com.squareup.otto.Bus

class RoutePresenterImpl(private val routeEngine: RouteEngine,
        private val routeEngineListener: RouteEngineListener,
        private val bus: Bus,
        private val vsm: ViewStateManager) : RoutePresenter {

    override var routeController: RouteViewController? = null
        set(value) {
            field = value
            routeEngineListener.controller = value
        }

    override var currentInstructionIndex: Int = 0
    override var currentSnapLocation: Location? = null

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
        isTrackingCurrentLocation = true
    }

    override fun onRouteResume(route: Route?) {
        routeController?.setCurrentInstruction(currentInstructionIndex)
        if (!isTrackingCurrentLocation) {
            routeController?.showResumeButton()
        }
        val location = route?.getRouteInstructions()?.get(currentInstructionIndex)?.location
        if (location != null) {
            routeController?.showRouteIcon(location)
        }
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
            vsm.viewState = ViewStateManager.ViewState.ROUTE_DIRECTION_LIST
        } else {
            routeController?.hideRouteDirectionList()
            vsm.viewState = ViewStateManager.ViewState.ROUTING
        }
    }

    override fun onRouteCancelButtonClick() {
        bus.post(RouteCancelEvent())
    }

    override fun isTrackingCurrentLocation(): Boolean {
        return isTrackingCurrentLocation
    }
    
    /**
     * When we center the map on a location we want to dynamically change the map's zoom level.
     * We will zoom the map out if the current maneuver is long and we are still far away from
     * completing it. If the maneuver is relatively short we won't adjust the zoom level which will
     * prevent changing the zoom too frequently
     */
    override fun mapZoomLevelForCurrentInstruction(): Float  {
        var liveDistanceThreshold: Int = 0
        var distanceThreshold: Int = 0
                when (route?.units) {
            Router.DistanceUnits.MILES -> {
                liveDistanceThreshold = (Instruction.MI_TO_METERS * 2).toInt()
                distanceThreshold = (Instruction.MI_TO_METERS * 3).toInt()
            }
            Router.DistanceUnits.KILOMETERS -> {
                liveDistanceThreshold = Instruction.KM_TO_METERS * 3
                distanceThreshold = Instruction.KM_TO_METERS * 3
            }
        }

        val instruction = route?.getCurrentInstruction()
        if (instruction != null && instruction.liveDistanceToNext > liveDistanceThreshold
                && instruction.distance > distanceThreshold) {
            return MainPresenter.LONG_MANEUVER_ZOOM
        } else {
            return MainPresenter.ROUTING_ZOOM
        }
    }

    override fun onSetCurrentInstruction(index: Int) {
        val finalInstructionIndex = route?.getRouteInstructions()?.size?.minus(1)
        if (index != finalInstructionIndex) routeController?.displayInstruction(index)
    }
}
