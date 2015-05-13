package com.mapzen.erasermap

import android.content.Context
import android.util.Log
import com.squareup.okhttp.HttpResponseCache
import java.io.File
import java.io.IOException

public class TileCacheBuilder(val context: Context) {
    public val CACHE_DIR: String = "tile-cache"
    public val CACHE_SIZE: Long = 1024 * 1024 * 10 // 10 Megs

    fun build(): HttpResponseCache? {
        val externalCacheDir = context.getExternalCacheDir()
        if (externalCacheDir != null) {
            try {
                return HttpResponseCache(File(externalCacheDir, CACHE_DIR), CACHE_SIZE)
            } catch (e: IOException) {
                Log.e(PrivateMapsApplication.TAG, "Unable to create tile cache", e)
            }
        }

        return null
    }
}
