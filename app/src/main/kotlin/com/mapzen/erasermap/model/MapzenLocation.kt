package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.pelias.PeliasLocationProvider

public interface MapzenLocation : PeliasLocationProvider {
    public fun connect()
    public fun disconnect()
    public fun isConnected(): Boolean
    public fun initLocationUpdates()
    public fun getLastLocation(): Location?
}
