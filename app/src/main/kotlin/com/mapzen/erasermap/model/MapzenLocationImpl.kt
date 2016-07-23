package com.mapzen.erasermap.model

import android.content.Context
import android.graphics.PointF
import android.location.Location
import android.util.Log
import android.view.WindowManager
import com.mapzen.android.MapzenMap
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.model.event.LocationChangeEvent
import com.mapzen.pelias.BoundingBox
import com.squareup.otto.Bus

public class MapzenLocationImpl(val locationClient: LostApiClient,
        val settings: AppSettings,
        val bus: Bus,
        val application: EraserMapApplication,
        val permissionManager: PermissionManager) : MapzenLocation {

    companion object {
        private val LOCATION_UPDATE_INTERVAL_IN_MS: Long = 1000L
        private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT: Float = 3f
    }

    init {
        bus.register(this)
    }

    override var mapzenMap: MapzenMap? = null

    private var previousLocation: Location? = null

    private fun connect() {
        if (!locationClient.isConnected) {
            locationClient.connect()
        }
        if (settings.isMockLocationEnabled) {
            LocationServices.FusedLocationApi?.setMockMode(true)
            LocationServices.FusedLocationApi?.setMockLocation(settings.mockLocation)
        }
        if (settings.isMockRouteEnabled) {
            LocationServices.FusedLocationApi?.setMockTrace(settings.mockRoute)
        }
    }

    private fun disconnect() {
        locationClient.disconnect()
    }

    override fun getLastLocation(): Location? {
        if (!permissionManager.permissionsGranted()) {
            return null
        }
        connect()
        return LocationServices.FusedLocationApi?.lastLocation
    }

    override fun startLocationUpdates() {
        if (!permissionManager.permissionsGranted()) {
            return
        }
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
            if (BuildConfig.DEBUG) {
                Log.d("MapzenLocation", "onLocationChanged: " + location)
            }
            bus.post(LocationChangeEvent(location))
        } else {
            if (BuildConfig.DEBUG) {
                Log.d("MapzenLocation", "no significant change")
            }
        }

        previousLocation = location
    }

    override fun stopLocationUpdates() {
        disconnect()
    }

    override fun getLat(): Double {
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val midLatLon = mapzenMap?.screenPositionToLngLat(PointF(display.width.toFloat() / 2,
            display.height.toFloat() / 2))
        if (midLatLon?.latitude == null) {
            return 0.0
        }
        return midLatLon?.latitude as Double
    }

    override fun getLon(): Double {
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val midLatLon = mapzenMap?.screenPositionToLngLat(PointF(display.width.toFloat()/2,
            display.height.toFloat()/2))
        if (midLatLon?.longitude == null) {
            return 0.0
        }
        return midLatLon?.longitude as Double
    }

    override fun getBoundingBox(): BoundingBox? {
        val windowManager = application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val display = windowManager.defaultDisplay
        val minLatLon = mapzenMap?.screenPositionToLngLat(PointF(0.0f, display.height.toFloat()))
        val maxLatLon = mapzenMap?.screenPositionToLngLat(PointF(display.width.toFloat(), 0.0f))
        val boundingBox: BoundingBox = BoundingBox(
                minLatLon?.latitude as Double,
                minLatLon?.longitude as Double,
                maxLatLon?.latitude as Double,
                maxLatLon?.longitude as Double)
        return boundingBox
    }
}
