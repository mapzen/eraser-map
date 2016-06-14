package com.mapzen.erasermap.model

import com.mapzen.erasermap.presenter.MainPresenter

class ConfidenceHandler(val presenter: MainPresenter) {

    var longPressed = false;

    companion object {
        const val CONFIDENCE_THRESHOLD = 0.8
        const val CONFIDENCE_MISSING = -1.0
    }

    fun useRawLatLng(confidence: Double): Boolean {
        if (confidence == CONFIDENCE_MISSING || !longPressed) {
            return false;
        }
        return confidence < CONFIDENCE_THRESHOLD
                && presenter.reverseGeoLngLat != null
    }

}
