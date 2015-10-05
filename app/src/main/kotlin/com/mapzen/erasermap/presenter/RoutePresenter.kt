package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.helpers.RouteListener
import com.mapzen.valhalla.Route

public interface RoutePresenter {
    public var routeController: RouteViewController?
    public var routeListener: RouteListener?

    public fun onLocationChanged(location: Location)
    public fun setRoute(route: Route?)
    public fun onMapGesture()
    public fun onResumeButtonClick()
}
