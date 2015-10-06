package com.mapzen.erasermap.model

import android.location.Location
import android.util.Log
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

public class MapzenLocationImpl(val locationClient: LostApiClient,
        val settings: AppSettings,
        val bus: Bus) : MapzenLocation {

    companion object {
        private val LOCATION_UPDATE_INTERVAL_IN_MS: Long = 1000L
        private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT: Float = 0f
    }

    init {
        bus.register(this)
    }

    private fun connect() {
        if (!locationClient.isConnected) {
            locationClient.connect()
        }

        if (settings.isMockLocationEnabled) {
            LocationServices.FusedLocationApi?.setMockMode(true)
            LocationServices.FusedLocationApi?.setMockLocation(settings.mockLocation)
        }
    }

    private fun disconnect() {
        locationClient.disconnect()
    }

    override fun getLastLocation(): Location? {
        connect()
        return LocationServices.FusedLocationApi?.lastLocation
    }

    override fun startLocationUpdates() {
        connect()
        val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL_IN_MS)
                .setFastestInterval(LOCATION_UPDATE_INTERVAL_IN_MS)
                .setSmallestDisplacement(LOCATION_UPDATE_SMALLEST_DISPLACEMENT)

        LocationServices.FusedLocationApi?.requestLocationUpdates(locationRequest) {
            location: Location -> bus.post(LocationChangeEvent(location))
            Log.d("MapzenLocation", "onLocationChanged: " + location)
        }
    }

    override fun stopLocationUpdates() {
        disconnect()
    }

    @Subscribe public fun onRouteEvent(event: RouteEvent) {
        if (settings.isMockRouteEnabled) {
            LocationServices.FusedLocationApi?.setMockMode(true)
            LocationServices.FusedLocationApi?.setMockTrace(settings.mockRoute)
            startLocationUpdates()
        }
    }

    override fun getLon(): String {
        connect()
        return LocationServices.FusedLocationApi?.lastLocation?.longitude.toString()
    }

    override fun getLat(): String {
        connect()
        return LocationServices.FusedLocationApi?.lastLocation?.latitude.toString()
    }
}
