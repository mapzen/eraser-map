package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.erasermap.dummy.TestHelper

public class TestMapzenLocation : MapzenLocation {
    public var connected = false

    override fun startLocationUpdates() {
        connected = true
    }

    override fun stopLocationUpdates() {
        connected = false
    }

    override fun getLastLocation(): Location? {
        return TestHelper.getTestLocation()
    }

    override fun getLon(): String? {
        return "0.0"
    }

    override fun getLat(): String? {
        return "0.0"
    }
}
