package com.mapzen.erasermap.model

import com.mapzen.android.lost.api.Status

class TestLostSettingsChecker: LocationSettingsChecker {

  override fun getLocationStatus(mapzenLocation: MapzenLocation,
      locationClientManager: LocationClientManager): Status {
    return Status(Status.SUCCESS)
  }

  override fun getLocationStatusCode(mapzenLocation: MapzenLocation,
      locationClientManager: LocationClientManager): Int {
    return Status.SUCCESS
  }

}
