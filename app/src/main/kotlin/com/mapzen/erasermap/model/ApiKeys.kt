package com.mapzen.erasermap.model

import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.EraserMapApplication

/**
 * Wrangle API keys for Mapzen services
 */
class ApiKeys private constructor(val application: EraserMapApplication) {

    lateinit var apiKey: String

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
        if (apiKey.isEmpty()) throw IllegalArgumentException("Api key cannot be empty.")
    }

    private fun configureKeys() {
        apiKey = BuildConfig.API_KEY
    }
}
