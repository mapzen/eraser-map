package com.mapzen.erasermap.model

import android.content.Context
import com.mapzen.android.MapzenRouter
import com.mapzen.valhalla.RouteCallback
import java.util.ArrayList

public class TestRouter(context: Context) : MapzenRouter(context) {
    public var locations: ArrayList<DoubleArray> = ArrayList()
    public var isFetching: Boolean = false
    public var units: MapzenRouter.DistanceUnits = MapzenRouter.DistanceUnits.MILES
    public var bearing: Float = 0f
    public var name: String? = null

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

    override fun setLocation(point: DoubleArray, heading: Float): MapzenRouter {
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
