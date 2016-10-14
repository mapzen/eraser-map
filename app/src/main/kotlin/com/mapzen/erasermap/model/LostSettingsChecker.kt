package com.mapzen.erasermap.model

import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LocationSettingsRequest
import com.mapzen.android.lost.api.Status

class LostSettingsChecker: LocationSettingsChecker {
  override fun getLocationStatus(mapzenLocation: MapzenLocation,
      locationClientManager: LocationClientManager): Status {
    val locationRequest = mapzenLocation.getLocationRequest()
    val settingsRequest = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        .build()
    val pendingResult = LocationServices.SettingsApi.checkLocationSettings(
        locationClientManager.getClient(), settingsRequest)
    val locationSettingsResult = pendingResult.await()
    return locationSettingsResult.status
  }

  override fun getLocationStatusCode(mapzenLocation: MapzenLocation,
      locationClientManager: LocationClientManager): Int {
    return getLocationStatus(mapzenLocation, locationClientManager).statusCode
  }

}
