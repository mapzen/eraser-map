package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.view.MapListToggleButton
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.model.ValhallaLocation
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route

interface RoutePresenter {
    companion object {
        val GESTURE_MIN_DELTA = 0.0001f
    }

    var routeController: RouteViewController?
    var currentInstructionIndex: Int
    var currentSnapLocation: ValhallaLocation?

    fun onLocationChanged(location: ValhallaLocation)
    fun onRouteStart(route: Route?)
    fun onRouteResume(route: Route?)
    fun onRouteResumeForMap(route: Route?)
    fun onMapPan(deltaX: Float, deltaY: Float)
    fun onResumeButtonClick()
    fun onInstructionPagerTouch()
    fun onInstructionSelected(instruction: Instruction)
    fun onUpdateSnapLocation(location: ValhallaLocation)
    fun onRouteClear()
    fun onMapListToggleClick(state: MapListToggleButton.MapListState)
    fun onRouteCancelButtonClick()
    fun mapZoomLevelForCurrentInstruction(): Float
    fun isTrackingCurrentLocation(): Boolean
    fun onSetCurrentInstruction(index: Int)
}
