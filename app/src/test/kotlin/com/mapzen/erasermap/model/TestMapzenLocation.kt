package com.mapzen.erasermap.model

import android.location.Location

public class TestMapzenLocation : MapzenLocation {
    private var connected = false;

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
        throw UnsupportedOperationException()
    }

    override fun getLastLocation(): Location? {
        throw UnsupportedOperationException()
    }

    override fun getLon(): String? {
        throw UnsupportedOperationException()
    }

    override fun getLat(): String? {
        throw UnsupportedOperationException()
    }
}
