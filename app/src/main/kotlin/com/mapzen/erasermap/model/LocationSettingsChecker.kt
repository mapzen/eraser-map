package com.mapzen.erasermap.model

import com.mapzen.android.lost.api.Status

interface LocationSettingsChecker {
  fun getLocationStatus(mapzenLocation: MapzenLocation,
      locationClientManager: LocationClientManager): Status
  fun getLocationStatusCode(mapzenLocation: MapzenLocation,
      locationClientManager: LocationClientManager): Int
}
