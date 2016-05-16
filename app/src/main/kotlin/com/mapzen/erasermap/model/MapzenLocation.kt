package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.android.MapzenMap
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.tangram.MapController

public interface MapzenLocation : PeliasLocationProvider {
    public var mapzenMap: MapzenMap?
    public fun getLastLocation(): Location?
    public fun startLocationUpdates()
    public fun stopLocationUpdates()
}
