package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.erasermap.dummy.TestHelper

public class TestMapzenLocation : MapzenLocation {
    public var connected = false
    public var updates = false

    override fun connect() {
        connected = true
    }

    override fun disconnect() {
        connected = false
        updates = false
    }

    override fun isConnected(): Boolean {
        return connected
    }

    override fun initLocationUpdates() {
        connected = true
        updates = true
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
