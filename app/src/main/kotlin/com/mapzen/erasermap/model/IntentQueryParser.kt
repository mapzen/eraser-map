package com.mapzen.erasermap.model

import com.mapzen.tangram.LngLat

/**
 * Parses incoming query strings that may contain a name, address, and/or latitude and longitude
 * to be displayed on the map.
 *
 * Some sample formats include:
 *     * geo:0,0?q=350 5th Ave, New York, NY 10118
 *     * http://maps.google.com/maps?q=350 5th Ave, New York, NY 10118, United States&sll=40.7484,-73.9857&radius=5
 */
open class IntentQueryParser {

    /**
     * Parses the query string portion of the incoming location data (everything after the "?q=").
     */
    open fun parse(query: String): IntentQuery? {
        val lngLat = LngLat()

        if (query.contains("sll=")) {
            val sll = query.split("sll=")
            if (sll.size > 1) {
                val raw = sll[1].split("&")[0]
                lngLat.latitude = raw.split(",")[0].toDouble()
                lngLat.longitude = raw.split(",")[1].toDouble()
            }
        }

        val split = query.split("q=")
        if (split.size > 1) {
            return IntentQuery(split[1].split("&")[0], lngLat)
        }

        return null
    }
}
