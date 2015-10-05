package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.helpers.RouteEngine
import com.mapzen.helpers.RouteListener
import com.mapzen.valhalla.Route

public class RoutePresenterImpl(val routeEngine: RouteEngine) : RoutePresenter {
    override var routeController: RouteViewController? = null

    override var routeListener: RouteListener? = null
        set(value) {
            routeEngine.setListener(value)
        }

    override fun onLocationChanged(location: Location) {
        routeEngine.onLocationChanged(location)
    }

    override fun setRoute(route: Route?) {
        if (routeEngine.route == null) {
            routeEngine.route = route
        }
    }

    override fun onMapGesture() {
        routeController?.showResumeButton()
    }

    override fun onResumeButtonClick() {
        routeController?.hideResumeButton()
    }
}
