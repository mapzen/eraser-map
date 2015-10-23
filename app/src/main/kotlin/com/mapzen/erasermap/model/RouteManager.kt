package com.mapzen.erasermap.model

import com.mapzen.valhalla.Router

public interface RouteManager {
    public var apiKey: String
    public fun getInitializedRouter(type: Router.Type): Router
}
