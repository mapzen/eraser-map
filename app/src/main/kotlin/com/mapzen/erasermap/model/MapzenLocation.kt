package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.pelias.PeliasLocationProvider
import javax.inject.Inject

public class MapzenLocation (val app: EraserMapApplication) : PeliasLocationProvider {
    private val LOCATION_UPDATE_INTERVAL_IN_MS: Long = 1000L
    private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT: Float = 0f

    var locationClient: LostApiClient? = null
        @Inject set

    init {
        app.component()?.inject(this)
    }

    public fun connect() {
        locationClient?.connect()
    }

    public fun disconnect() {
        locationClient?.disconnect()
    }

    public fun initLocationUpdates(callback: (location: Location) -> Unit) {
        if (locationClient?.isConnected() == false) {
            locationClient?.connect()
        }

        val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL_IN_MS)
                .setFastestInterval(LOCATION_UPDATE_INTERVAL_IN_MS)
                .setSmallestDisplacement(LOCATION_UPDATE_SMALLEST_DISPLACEMENT)

        LocationServices.FusedLocationApi?.requestLocationUpdates(locationRequest) {
            location: Location -> callback(location)
        }
    }

    public fun getLastLocation(): Location? {
        return LocationServices.FusedLocationApi?.getLastLocation()
    }

    override fun getLon(): String? {
        return LocationServices.FusedLocationApi?.getLastLocation()?.getLongitude().toString()
    }

    override fun getLat(): String? {
        return LocationServices.FusedLocationApi?.getLastLocation()?.getLatitude().toString()
    }
}
