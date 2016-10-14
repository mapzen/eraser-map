package com.mapzen.erasermap.model

import android.content.Context
import android.graphics.PointF
import android.location.Location
import android.util.Log
import android.view.WindowManager
import com.mapzen.android.graphics.MapzenMap
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.model.event.LocationChangeEvent
import com.mapzen.pelias.BoundingBox
import com.squareup.otto.Bus

public class MapzenLocationImpl(val locationClientManager: LocationClientManager,
        val settings: AppSettings,
        val bus: Bus,
        val application: EraserMapApplication,
        val permissionManager: PermissionManager) : MapzenLocation {

    companion object {
        private val LOCATION_UPDATE_INTERVAL_IN_MS: Long = 1000L
        private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT: Float = 3f
    }

    private val request = LocationRequest.create()
        .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        .setInterval(LOCATION_UPDATE_INTERVAL_IN_MS)
        .setFastestInterval(LOCATION_UPDATE_INTERVAL_IN_MS)
        .setSmallestDisplacement(LOCATION_UPDATE_SMALLEST_DISPLACEMENT)

    init {
        bus.register(this)
    }

    override var mapzenMap: MapzenMap? = null

    private var previousLocation: Location? = null

    private fun connect() {
        val locationClient = locationClientManager.getClient()
        if (settings.isMockLocationEnabled) {
            LocationServices.FusedLocationApi?.setMockMode(locationClient, true)
            LocationServices.FusedLocationApi?.setMockLocation(locationClient, settings.mockLocation)
        }
        if (settings.isMockRouteEnabled) {
            LocationServices.FusedLocationApi?.setMockTrace(locationClient, settings.mockRoute)
        }
    }

    private fun disconnect() {
        locationClientManager.disconnect()
    }

    override fun getLastLocation(): Location? {
        if (!permissionManager.permissionsGranted()) {
            return null
        }
        connect()
        val client = locationClientManager.getClient()
        return LocationServices.FusedLocationApi?.getLastLocation(client)
    }

    override fun startLocationUpdates() {
        if (!permissionManager.permissionsGranted()) {
            return
        }
        connect()
        val client = locationClientManager.getClient()
        LocationServices.FusedLocationApi?.requestLocationUpdates(client, request,
            object: com.mapzen.android.lost.api.LocationListener {
                override fun onLocationChanged(location: Location) {
                    onLocationUpdate(location)
                }

                override fun onProviderDisabled(provider: String) {

                }

                override fun onProviderEnabled(provider: String) {

                }
            }
        )
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

    override fun getLocationRequest(): LocationRequest {
        return request
    }
}
