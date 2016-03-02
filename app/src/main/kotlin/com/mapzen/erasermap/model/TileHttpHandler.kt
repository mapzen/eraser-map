package com.mapzen.erasermap.model

import android.app.Application
import android.os.Build
import com.mapzen.tangram.HttpHandler
import okhttp3.Callback
import java.io.File

public class TileHttpHandler(application: Application) : HttpHandler() {
    companion object {
        @JvmStatic val KITKAT = 19
    }

    init {
        if (Build.VERSION.SDK_INT >= KITKAT) {
            val httpCache = File(application.externalCacheDir.absolutePath + "/tile_cache")
            setCache(httpCache, 30 * 1024 * 1024)
        }
    }

    var apiKey: String? = null

    override fun onRequest(url: String, callback: Callback): Boolean {
        val urlWithApiKey = url + "?api_key=" + apiKey
        return super.onRequest(urlWithApiKey, callback)
    }

    override fun onCancel(url: String) {
        val urlWithApiKey = url + "?api_key=" + apiKey
        super.onCancel(urlWithApiKey)
    }
}
