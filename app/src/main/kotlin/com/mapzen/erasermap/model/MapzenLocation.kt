package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.pelias.PeliasLocationProvider

public interface MapzenLocation : PeliasLocationProvider {
    public fun getLastLocation(): Location?
    public fun startLocationUpdates()
    public fun stopLocationUpdates()
}
