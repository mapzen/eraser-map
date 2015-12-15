package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.valhalla.Router
import java.io.File

public interface AppSettings {
    public var distanceUnits: Router.DistanceUnits
    public var isMockLocationEnabled: Boolean
    public var mockLocation: Location
    public var isMockRouteEnabled: Boolean
    public var mockRoute: File
    public var isTileDebugEnabled: Boolean
    public var isLabelDebugEnabled: Boolean

    public fun initTangramDebugFlags()
}
