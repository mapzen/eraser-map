package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.model.ValhallaLocation

class LocationConverter {

  fun mapzenLocation(location: Location):ValhallaLocation {
    val mapzenLocation = ValhallaLocation()
    mapzenLocation.bearing = location.bearing
    mapzenLocation.latitude = location.latitude
    mapzenLocation.longitude = location.longitude
    return mapzenLocation
  }
}
