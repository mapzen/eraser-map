package com.mapzen.erasermap.model

import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router

interface RouteManager {
    var origin: ValhallaLocation?
    var destination: Feature?
    var type: Router.Type
    var reverse: Boolean
    var route: Route?
    var bearing: Float?
    var currentRequest: RouteCallback?

    fun fetchRoute(callback: RouteCallback)
    fun toggleReverse()
}
