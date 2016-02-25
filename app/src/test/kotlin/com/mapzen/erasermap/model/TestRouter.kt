package com.mapzen.erasermap.model

import com.mapzen.valhalla.JSON
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import retrofit.RestAdapter
import java.util.ArrayList

public class TestRouter : Router {
    public var locations: ArrayList<DoubleArray> = ArrayList()
    public var isFetching: Boolean = false
    public var units: Router.DistanceUnits = Router.DistanceUnits.MILES
    public var bearing: Float = 0f
    public var name: String? = null
    public var logLevel: RestAdapter.LogLevel = RestAdapter.LogLevel.BASIC

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

    override fun setLocation(point: DoubleArray, heading: Float): Router {
        locations.add(point)
        bearing = heading
        return this
    }

    override fun setLocation(point: DoubleArray, name: String?, street: String?, city: String?,
            state: String?): Router {
        this.name = name
        locations.add(point)
        return this
    }

    override fun setWalking(): Router {
        return this
    }

    override fun setDistanceUnits(units: Router.DistanceUnits): Router {
        this.units = units
        return this
    }

    override fun setLogLevel(logLevel: RestAdapter.LogLevel): Router {
        this.logLevel = logLevel
        return this
    }
}
