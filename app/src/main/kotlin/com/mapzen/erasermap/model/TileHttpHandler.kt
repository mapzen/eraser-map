package com.mapzen.erasermap.model

import okhttp3.Callback
import java.io.File

class TileHttpHandler : TmpHttpHandler {
    constructor() : super() {}

    constructor(cacheDir: File, cacheSize: Long) : super(cacheDir, cacheSize) {}

    override fun onRequest(url: String, headers: Map<String, String>, callback: Callback): Boolean {
        val emHeaders = mutableMapOf(Http.HEADER_DNT to Http.VALUE_HEADER_DNT)
        emHeaders.putAll(headers)
        return super.onRequest(url, emHeaders, callback)
    }
}
