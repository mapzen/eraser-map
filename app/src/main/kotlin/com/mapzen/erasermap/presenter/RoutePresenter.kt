package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.view.MapListToggleButton
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route

public interface RoutePresenter {
    companion object {
        public val GESTURE_MIN_DELTA = 0.0001f
    }

    public var routeController: RouteViewController?
    public var currentInstructionIndex: Int

    public fun onLocationChanged(location: Location)
    public fun onRouteStart(route: Route?)
    public fun onRouteResume(route: Route?)
    public fun onMapPan(deltaX: Float, deltaY: Float)
    public fun onResumeButtonClick()
    public fun onInstructionPagerTouch()
    public fun onInstructionSelected(instruction: Instruction)
    public fun onUpdateSnapLocation(location: Location)
    public fun onRouteClear()
    public fun onMapListToggleClick(state: MapListToggleButton.MapListState)
    public fun onRouteCancelButtonClick()
    public fun mapZoomLevelForCenterMapOnLocation(location: Location): Float
    public fun isTrackingCurrentLocation(): Boolean
}
