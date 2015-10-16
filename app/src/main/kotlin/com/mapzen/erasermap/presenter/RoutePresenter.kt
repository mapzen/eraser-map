package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route

public interface RoutePresenter {
    public var routeController: RouteViewController?

    public fun onLocationChanged(location: Location)
    public fun onRouteStart(route: Route?)
    public fun onMapGesture()
    public fun onResumeButtonClick()
    public fun onInstructionPagerTouch()
    public fun onInstructionSelected(instruction: Instruction)
    public fun onUpdateSnapLocation(location: Location)
}
