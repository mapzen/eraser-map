package com.mapzen.erasermap.model

import android.content.Context
import com.mapzen.android.routing.MapzenRouter
import com.mapzen.valhalla.RouteCallback
import java.util.ArrayList

class TestRouter(context: Context) : MapzenRouter(context) {
    var locations: ArrayList<DoubleArray> = ArrayList()
    var isFetching: Boolean = false
    var units: MapzenRouter.DistanceUnits = MapzenRouter.DistanceUnits.MILES
    var bearing: Int = 0
    var name: String? = null

    override fun clearLocations(): MapzenRouter {
        locations.clear()
        return this
    }

    override fun fetch() {
        isFetching = true
    }

    override fun setBiking(): MapzenRouter {
        return this
    }

    override fun setCallback(callback: RouteCallback): MapzenRouter {
        return this
    }

    override fun setDriving(): MapzenRouter {
        return this
    }

    override fun setLocation(point: DoubleArray): MapzenRouter {
        locations.add(point)
        return this
    }

    override fun setLocation(point: DoubleArray, heading: Int): MapzenRouter {
        locations.add(point)
        bearing = heading
        return this
    }

    override fun setLocation(point: DoubleArray, name: String?, street: String?, city: String?,
            state: String?): MapzenRouter {
        this.name = name
        locations.add(point)
        return this
    }

    override fun setWalking(): MapzenRouter {
        return this
    }

    override fun setDistanceUnits(units: MapzenRouter.DistanceUnits): MapzenRouter {
        this.units = units
        return this
    }
}
