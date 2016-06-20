package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.android.MapzenMap
import com.mapzen.pelias.PeliasLocationProvider

interface MapzenLocation : PeliasLocationProvider {
    var mapzenMap: MapzenMap?
    fun getLastLocation(): Location?
    fun startLocationUpdates()
    fun stopLocationUpdates()
}
