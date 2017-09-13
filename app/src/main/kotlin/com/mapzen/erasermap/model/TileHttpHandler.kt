package com.mapzen.erasermap.model

import com.mapzen.android.graphics.MapzenMapHttpHandler
import java.io.File

class TileHttpHandler : MapzenMapHttpHandler {
    override fun headersForRequest(): MutableMap<String, String> {
        return mutableMapOf(Http.HEADER_DNT to Http.VALUE_HEADER_DNT)
    }

    override fun queryParamsForRequest(): MutableMap<String, String> {
        return mutableMapOf()
    }

    constructor() : super()

    constructor(cacheDir: File, cacheSize: Long) : super(cacheDir, cacheSize)
}
