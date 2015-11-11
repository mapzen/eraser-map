package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.tangram.MapController

public interface MapzenLocation : PeliasLocationProvider {
    public var mapController: MapController?
    public fun getLastLocation(): Location?
    public fun startLocationUpdates()
    public fun stopLocationUpdates()
}
