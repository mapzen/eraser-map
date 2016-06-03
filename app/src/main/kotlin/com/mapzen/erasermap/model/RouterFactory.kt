package com.mapzen.erasermap.model

import android.content.Context
import com.mapzen.android.MapzenRouter

interface RouterFactory {
    public fun getRouter(context: Context): MapzenRouter
}
