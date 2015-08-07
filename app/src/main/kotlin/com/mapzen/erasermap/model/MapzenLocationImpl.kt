package com.mapzen.erasermap.model

import android.content.SharedPreferences
import android.location.Location
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import javax.inject.Inject

public class MapzenLocationImpl(val app: EraserMapApplication) : MapzenLocation {
    private val LOCATION_UPDATE_INTERVAL_IN_MS: Long = 1000L
    private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT: Float = 0f

    var locationClient: LostApiClient? = null
        @Inject set
    var prefs: SharedPreferences? = null
        @Inject set

    init {
        app.component()?.inject(this)
    }

    override fun connect() {
        locationClient?.connect()
        val mockMode = prefs?.getBoolean(app.getString(R.string.checkbox_mock_location_key), false)
        if (mockMode as Boolean) {
            initMockMode(mockMode)
        }
    }

    private fun initMockMode(mockMode: Boolean) {
        val rawValues = prefs?.getString(app.getString(R.string.edittext_mock_location_key), null)
        val splitValues = rawValues?.split(",")
        val location = Location("mock")
        location.setLatitude(splitValues?.get(0)?.toDouble() as Double)
        location.setLongitude(splitValues?.get(1)?.toDouble() as Double)
        LocationServices.FusedLocationApi?.setMockMode(mockMode)
        LocationServices.FusedLocationApi?.setMockLocation(location)
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
            connect()
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
