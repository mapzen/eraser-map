package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.pelias.PeliasLocationProvider
import javax.inject.Inject

public class MapzenLocationImpl(val app: EraserMapApplication) : MapzenLocation {
    private val LOCATION_UPDATE_INTERVAL_IN_MS: Long = 1000L
    private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT: Float = 0f

    var locationClient: LostApiClient? = null
        @Inject set

    init {
        app.component()?.inject(this)
    }

    override fun connect() {
        locationClient?.connect()
    }

    override fun disconnect() {
        locationClient?.disconnect()
    }

    override fun isConnected(): Boolean {
        val isConnected = locationClient?.isConnected()
        if (isConnected is Boolean) return isConnected else return false
    }

    override fun initLocationUpdates(callback: (location: Location) -> Unit) {
        if (!isConnected()) {
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

    override fun getLastLocation(): Location? {
        return LocationServices.FusedLocationApi?.getLastLocation()
    }

    override fun getLon(): String? {
        return LocationServices.FusedLocationApi?.getLastLocation()?.getLongitude().toString()
    }

    override fun getLat(): String? {
        return LocationServices.FusedLocationApi?.getLastLocation()?.getLatitude().toString()
    }
}
