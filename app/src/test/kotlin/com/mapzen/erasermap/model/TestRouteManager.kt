package com.mapzen.erasermap.model

import com.mapzen.erasermap.BuildConfig
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import org.json.JSONObject
import java.util.ArrayList

class TestRouteManager : RouteManager {
    override var origin: ValhallaLocation? = null
    override var destination: Feature? = null
    override var type: Router.Type = Router.Type.DRIVING
    override var reverse: Boolean = false
    override var route: Route? = null
    override var bearing: Float? = null
    override var currentRequest: RouteCallback? = null

    override fun fetchRoute(callback: RouteCallback) {
        route = TestRoute()
    }

    override fun toggleReverse() {
        reverse = !reverse
    }

    var locations: ArrayList<DoubleArray> = ArrayList()
    var isFetching: Boolean = false
    var units: Router.DistanceUnits = Router.DistanceUnits.MILES

    override var apiKey: String = BuildConfig.API_KEY

    fun reset() {
        locations.clear()
        origin = null
        destination = null
        route = null
        bearing = null
    }

    inner class TestRoute : Route(JSONObject()) {
    }
}
