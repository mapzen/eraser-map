package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.valhalla.Router
import java.io.File

public class TestAppSettings : AppSettings {
    override var distanceUnits: Router.DistanceUnits = Router.DistanceUnits.MILES
    override var isMockLocationEnabled: Boolean = false
    override var mockLocation: Location = TestHelper.getTestLocation()
    override var isMockRouteEnabled: Boolean = false
    override var mockRoute: File = File("lost.gpx")
    override var isTileDebugEnabled: Boolean = false
    override var isLabelDebugEnabled: Boolean = false
}
