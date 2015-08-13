package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.erasermap.dummy.TestHelper

public class TestMapzenLocation : MapzenLocation {
    private var connected = false;
    public var callback: ((Location) -> Unit)? = null

    override fun connect() {
        connected = true
    }

    override fun disconnect() {
        connected = false
    }

    override fun isConnected(): Boolean {
        return connected
    }

    override fun initLocationUpdates(callback: (Location) -> Unit) {
        this.callback = callback
    }

    override fun initRouteLocationUpdates(callback: (Location) -> Unit) {
        this.callback = callback
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
