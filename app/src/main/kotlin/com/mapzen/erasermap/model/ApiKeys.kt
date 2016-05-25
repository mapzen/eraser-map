package com.mapzen.erasermap.model

import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication

/**
 * Wrangle API keys for Mapzen services
 */
data class ApiKeys(val application: EraserMapApplication) {

    lateinit var tilesKey: String
    lateinit var searchKey: String
    lateinit var routingKey: String

    init {
        configureKeys()
        if (tilesKey.isEmpty()) throw IllegalArgumentException("Tiles key cannot be empty.")
        if (searchKey.isEmpty()) throw IllegalArgumentException("Search key cannot be empty.")
        if (routingKey.isEmpty()) throw IllegalArgumentException("Routing key cannot be empty.")
    }

    private fun configureKeys() {
        if (BuildConfig.DEBUG) {
            tilesKey = BuildConfig.VECTOR_TILE_API_KEY
            searchKey = BuildConfig.PELIAS_API_KEY
            routingKey = BuildConfig.VALHALLA_API_KEY
        } else {
            val crypt = SimpleCrypt(application)
            tilesKey = crypt.decode(BuildConfig.VECTOR_TILE_API_KEY)
            searchKey = crypt.decode(BuildConfig.PELIAS_API_KEY)
            routingKey = crypt.decode(BuildConfig.VALHALLA_API_KEY)
        }
    }
}
