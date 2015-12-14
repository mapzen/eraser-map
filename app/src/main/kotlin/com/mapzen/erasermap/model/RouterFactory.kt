package com.mapzen.erasermap.model

import com.mapzen.valhalla.Router

interface RouterFactory {
    public fun getRouter(): Router
}
