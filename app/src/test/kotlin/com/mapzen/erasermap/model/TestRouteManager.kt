package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.erasermap.BuildConfig
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.JSON
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import org.json.JSONObject
import java.util.ArrayList

public class TestRouteManager : RouteManager {
    override var origin: Location? = null
    override var destination: Feature? = null
    override var type: Router.Type = Router.Type.DRIVING
    override var reverse: Boolean = false
    override var route: Route? = null

    override fun fetchRoute(callback: RouteCallback) {
        route = TestRoute()
    }

    public var locations: ArrayList<DoubleArray> = ArrayList()
    public var isFetching: Boolean = false
    public var units: Router.DistanceUnits = Router.DistanceUnits.MILES

    override var apiKey: String = BuildConfig.VALHALLA_API_KEY

    private fun getInitializedRouter(type: Router.Type): Router {
        return TestRouter()
    }

    public fun reset() {
        locations.clear()
        origin = null
        destination = null
        route = null
    }

    public inner class TestRouter : Router {
        override fun clearLocations(): Router {
            locations.clear()
            return this
        }

        override fun fetch() {
            isFetching = true
        }

        override fun getEndpoint(): String {
            return ""
        }

        override fun getJSONRequest(): JSON {
            return JSON()
        }

        override fun setApiKey(key: String): Router {
            return this
        }

        override fun setBiking(): Router {
            return this
        }

        override fun setCallback(callback: RouteCallback): Router {
            return this
        }

        override fun setDriving(): Router {
            return this
        }

        override fun setEndpoint(url: String): Router {
            return this
        }

        override fun setLocation(point: DoubleArray): Router {
            locations.add(point)
            return this
        }

        override fun setLocation(point: DoubleArray, name: String?,
                street: String?, city: String?, state: String?): Router {
            locations.add(point)
            return this
        }

        override fun setWalking(): Router {
            return this
        }

        override fun setDistanceUnits(units: Router.DistanceUnits): Router {
            this@TestRouteManager.units = units
            return this
        }
    }

    public inner class TestRoute : Route(JSONObject()) {
    }
}
