package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router

public interface RouteManager {
    public var apiKey: String
    public var origin: ValhallaLocation?
    public var destination: Feature?
    public var type: Router.Type
    public var reverse: Boolean
    public var route: Route?
    public var bearing: Float?
    public fun fetchRoute(callback: RouteCallback)
    public fun toggleReverse()
}
