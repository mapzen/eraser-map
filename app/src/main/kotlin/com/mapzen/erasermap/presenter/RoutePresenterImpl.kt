package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.helpers.RouteEngine
import com.mapzen.helpers.RouteListener
import com.mapzen.valhalla.Route

public class RoutePresenterImpl(val routeEngine: RouteEngine,
        val mapzenLocation: MapzenLocation) : RoutePresenter {

    override var routeController: RouteViewController? = null

    override var routeListener: RouteListener? = null
        set(value) {
            routeEngine.setListener(value)
        }

    override fun onLocationChanged(location: Location) {
        routeEngine.onLocationChanged(location)
    }

    private var route: Route? = null

    override fun setRoute(route: Route?) {
        if (routeEngine.route == null) {
            this.route = route
            routeEngine.route = route
        }
    }

    override fun onMapGesture() {
        routeController?.showResumeButton()
    }

    override fun onResumeButtonClick() {
        routeController?.hideResumeButton()

        val location = mapzenLocation.getLastLocation()
        if (location is Location) {
            routeController?.centerMapOnLocation(location, getCurrentRotationInRadians())
        }
    }

    private fun getCurrentRotationInRadians(): Float {
        val bearingInDegrees = route?.getCurrentInstruction()?.bearing ?: 0
        return Math.toRadians(360 - bearingInDegrees.toDouble()).toFloat()
    }
}
