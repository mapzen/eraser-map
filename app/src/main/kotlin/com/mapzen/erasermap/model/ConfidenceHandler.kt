package com.mapzen.erasermap.model

import com.mapzen.tangram.LngLat

class ConfidenceHandler() {

    var longPressed = false
    var reverseGeoLngLat: LngLat? = null

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.8
        const val CONFIDENCE_MISSING = -1.0
    }

    fun useRawLatLng(confidence: Double): Boolean {
        if (confidence == CONFIDENCE_MISSING || !longPressed) {
            return false
        }

        return confidence < CONFIDENCE_THRESHOLD && reverseGeoLngLat != null
    }

}
