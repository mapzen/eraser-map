package com.mapzen.erasermap.model

import android.content.Context
import android.util.Log
import com.mapzen.erasermap.PrivateMapsApplication
import com.squareup.okhttp.Cache
import java.io.File
import java.io.IOException

public class TileCacheBuilder(val context: Context) {
    public val CACHE_DIR: String = "tile-cache"
    public val CACHE_SIZE: Long = 1024 * 1024 * 10 // 10 Megs

    fun build(): Cache? {
        val externalCacheDir = context.getExternalCacheDir()
        if (externalCacheDir != null) {
            try {
                return Cache(File(externalCacheDir, CACHE_DIR), CACHE_SIZE)
            } catch (e: IOException) {
                Log.e(PrivateMapsApplication.TAG, "Unable to create tile cache", e)
            }
        }

        return null
    }
}
