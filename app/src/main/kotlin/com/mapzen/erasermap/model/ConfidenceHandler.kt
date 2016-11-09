package com.mapzen.erasermap.model

import com.mapzen.tangram.LngLat

/**
 * Uses the following criteria to determine if raw lat/lng should be used for reverse geocode:
 *
 * * Pelias confidence score<
 * * Whether the reverse geocode was initiated by a long press
 * * Lat/lng coordinates returned in the reverse geocode result.
 */
class ConfidenceHandler() {

  var longPressed = false
  var reverseGeoLngLat: LngLat? = null

  companion object {
    const val CONFIDENCE_THRESHOLD = 0.8
    const val CONFIDENCE_MISSING = -1.0
  }

  /**
   * Returns true if the raw lat/lng values should be used. Otherwise false.
   */
  fun useRawLatLng(confidence: Double): Boolean {
    if (!hasConfidence(confidence) || !longPressed) {
      return false
    }

    return isConfidenceBelowThreshold(confidence) && hasReverseGeoLngLat()
  }

  private fun hasConfidence(confidence: Double) = confidence != CONFIDENCE_MISSING

  private fun isConfidenceBelowThreshold(confidence: Double) = confidence < CONFIDENCE_THRESHOLD

  private fun hasReverseGeoLngLat() = reverseGeoLngLat != null
}
