package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.valhalla.Router
import java.io.File

public interface AppSettings {
    companion object {
        public val DEFAULT_UNITS: Router.DistanceUnits = Router.DistanceUnits.KILOMETERS
    }

    public var distanceUnits: Router.DistanceUnits
    public var isMockLocationEnabled: Boolean
    public var mockLocation: Location
    public var isMockRouteEnabled: Boolean
    public var mockRoute: File
}
