package com.mapzen.erasermap.util

import android.content.Context
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.ConfidenceHandler
import com.mapzen.pelias.SimpleFeature

/**
 * Helper class used to display the name of a feature in the route preview view and in the
 * reverse geocode info view.
 */
class FeatureDisplayHelper(val context: Context, val confidenceHandler: ConfidenceHandler) {

  fun getDisplayName(feature: SimpleFeature): String {
    if (confidenceHandler.useRawLatLng(feature.confidence()) || feature.name().isBlank()) {
      return context.getString(R.string.dropped_pin)
    } else {
      return feature.name()
    }
  }
}
