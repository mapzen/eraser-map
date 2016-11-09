package com.mapzen.erasermap.model

import com.mapzen.tangram.LngLat
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class ConfidenceHandlerTest {
  val confidenceHandler = ConfidenceHandler()

  @Test fun shouldNotBeNull() {
    assertThat(confidenceHandler).isNotNull()
  }

  @Test fun useRawLatLng_shouldReturnFalseIfMissingConfidenceScore() {
    assertThat(confidenceHandler.useRawLatLng(ConfidenceHandler.CONFIDENCE_MISSING)).isFalse()
  }

  @Test fun useRawLatLng_shouldReturnFalseIfNotOriginatedFromLongPress() {
    confidenceHandler.longPressed = false
    assertThat(confidenceHandler.useRawLatLng(ConfidenceHandler.CONFIDENCE_THRESHOLD)).isFalse()
  }

  @Test fun useRawLatLng_shouldReturnFalseIfConfidenceIsNotLessThanThresholdValue() {
    confidenceHandler.longPressed = true
    confidenceHandler.reverseGeoLngLat = LngLat(0.0, 0.0)
    assertThat(confidenceHandler.useRawLatLng(ConfidenceHandler.CONFIDENCE_THRESHOLD)).isFalse()
  }

  @Test fun useRawLatLng_shouldReturnFalseIfReverseGeoLngLatIsNull() {
    confidenceHandler.longPressed = true
    confidenceHandler.reverseGeoLngLat = null
    assertThat(confidenceHandler.useRawLatLng(ConfidenceHandler.CONFIDENCE_THRESHOLD)).isFalse()
  }

  @Test fun useRawLatLng_shouldReturnTrueIfAllConditionsAreMet() {
    confidenceHandler.longPressed = true
    confidenceHandler.reverseGeoLngLat = LngLat(0.0, 0.0)
    assertThat(confidenceHandler.useRawLatLng(ConfidenceHandler.CONFIDENCE_THRESHOLD - 1)).isTrue()
  }
}
