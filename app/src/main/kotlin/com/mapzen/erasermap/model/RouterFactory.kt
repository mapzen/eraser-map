package com.mapzen.erasermap.model

import android.content.Context
import com.mapzen.android.routing.MapzenRouter

interface RouterFactory {
    public fun getRouter(context: Context): MapzenRouter
}
