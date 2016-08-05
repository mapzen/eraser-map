package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.android.graphics.MapzenMap
import com.mapzen.pelias.BoundingBox
import org.mockito.Mockito

class TestMapzenLocation : MapzenLocation {
    var connected = false

    override var mapzenMap: MapzenMap? = null

    override fun startLocationUpdates() {
        connected = true
    }

    override fun stopLocationUpdates() {
        connected = false
    }

    override fun getLastLocation(): Location? {
        return Mockito.mock(Location::class.java)
    }

    override fun getLon(): Double {
        return 0.0
    }

    override fun getLat(): Double {
        return 0.0
    }

    override fun getBoundingBox(): BoundingBox {
        return BoundingBox(0.0, 0.0, 0.0, 0.0)
    }
}
