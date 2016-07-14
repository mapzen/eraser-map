package com.mapzen.erasermap.model

import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication

/**
 * Wrangle API keys for Mapzen services
 */
class ApiKeys private constructor(val application: EraserMapApplication) {

    lateinit var tilesKey: String
    lateinit var searchKey: String
    lateinit var routingKey: String

    companion object {
        var instance: ApiKeys? = null

        fun sharedInstance(application: EraserMapApplication): ApiKeys {
            if (instance == null) {
                instance = ApiKeys(application)
            }
            return instance as ApiKeys
        }
    }

    init {
        configureKeys()
        if (tilesKey.isEmpty()) throw IllegalArgumentException("Tiles key cannot be empty.")
        if (searchKey.isEmpty()) throw IllegalArgumentException("Search key cannot be empty.")
        if (routingKey.isEmpty()) throw IllegalArgumentException("Routing key cannot be empty.")
    }

    private fun configureKeys() {
        tilesKey = BuildConfig.VECTOR_TILE_API_KEY
        searchKey = BuildConfig.PELIAS_API_KEY
        routingKey = BuildConfig.VALHALLA_API_KEY
    }
}
