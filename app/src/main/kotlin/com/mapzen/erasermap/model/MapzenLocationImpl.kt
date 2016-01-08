package com.mapzen.erasermap.model

import android.content.Context
import android.location.Location
import android.util.Log
import android.view.WindowManager
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.pelias.BoundingBox
import com.mapzen.tangram.MapController
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

public class MapzenLocationImpl(val locationClient: LostApiClient,
        val settings: AppSettings,
        val bus: Bus,
        val application: EraserMapApplication) : MapzenLocation {

    companion object {
        private val LOCATION_UPDATE_INTERVAL_IN_MS: Long = 1000L
        private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT: Float = 3f
    }

    init {
        bus.register(this)
    }

    override var mapController: MapController? = null

    private var previousLocation: Location? = null

    private fun connect() {
        if (!locationClient.isConnected) {
            locationClient.connect()

            if (settings.isMockLocationEnabled) {
                LocationServices.FusedLocationApi?.setMockMode(true)
                LocationServices.FusedLocationApi?.setMockLocation(settings.mockLocation)
            }
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
            location -> onLocationUpdate(location)
        }
    }

    fun onLocationUpdate(location: Location) {
        val previous = previousLocation
        val displacement = if (previous != null) previous.distanceTo(location) else Float.MAX_VALUE

        if (displacement > LOCATION_UPDATE_SMALLEST_DISPLACEMENT) {
            Log.d("MapzenLocation", "onLocationChanged: " + location)
            bus.post(LocationChangeEvent(location))
        } else {
            Log.d("MapzenLocation", "no significant change")
        }

        previousLocation = location
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

    override fun getLat(): Double {
        connect()
        return LocationServices.FusedLocationApi?.lastLocation?.latitude ?: 0.0
    }

    override fun getLon(): Double {
        connect()
        return LocationServices.FusedLocationApi?.lastLocation?.longitude ?: 0.0
    }

    override fun getBoundingBox(): BoundingBox? {
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val minLatLon = mapController?.coordinatesAtScreenPosition(0.0, display.height.toDouble())
        val maxLatLon = mapController?.coordinatesAtScreenPosition(display.width.toDouble(), 0.0)
        val boundingBox: BoundingBox = BoundingBox(
                minLatLon?.latitude as Double,
                minLatLon?.longitude as Double,
                maxLatLon?.latitude as Double,
                maxLatLon?.longitude as Double)
        return boundingBox
    }
}
