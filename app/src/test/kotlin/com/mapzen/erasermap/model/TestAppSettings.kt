package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.valhalla.Router
import java.io.File

public class TestAppSettings : AppSettings {
    override var distanceUnits: Router.DistanceUnits
        get() = throw UnsupportedOperationException()
        set(value) {
        }

    override var isMockLocationEnabled: Boolean
        get() = throw UnsupportedOperationException()
        set(value) {
        }

    override var mockLocation: Location
        get() = throw UnsupportedOperationException()
        set(value) {
        }

    override var isMockRouteEnabled: Boolean
        get() = throw UnsupportedOperationException()
        set(value) {
        }

    override var mockRoute: File
        get() = throw UnsupportedOperationException()
        set(value) {
        }
}
