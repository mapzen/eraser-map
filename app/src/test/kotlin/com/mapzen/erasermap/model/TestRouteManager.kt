package com.mapzen.erasermap.model

import com.mapzen.erasermap.BuildConfig
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import org.json.JSONObject
import java.util.ArrayList

public class TestRouteManager : RouteManager {
    override var origin: ValhallaLocation? = null
    override var destination: Feature? = null
    override var type: Router.Type = Router.Type.DRIVING
    override var reverse: Boolean = false
    override var route: Route? = null
    override var bearing: Float? = null

    override fun fetchRoute(callback: RouteCallback) {
        route = TestRoute()
    }

    override fun toggleReverse() {
        reverse = !reverse
    }

    public var locations: ArrayList<DoubleArray> = ArrayList()
    public var isFetching: Boolean = false
    public var units: Router.DistanceUnits = Router.DistanceUnits.MILES

    override var apiKey: String = BuildConfig.VALHALLA_API_KEY

    public fun reset() {
        locations.clear()
        origin = null
        destination = null
        route = null
        bearing = null
    }

    public inner class TestRoute : Route(JSONObject()) {
    }
}
