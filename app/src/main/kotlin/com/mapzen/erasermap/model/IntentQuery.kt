package com.mapzen.erasermap.model

import com.mapzen.tangram.LngLat

/**
 * Represents components of an implicit intent query string that has been parsed.
 */
data class IntentQuery(val queryString: String, val focusPoint: LngLat)
