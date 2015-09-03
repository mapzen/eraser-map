package com.mapzen.erasermap.model

import com.mapzen.valhalla.Router

public interface RouterFactory {
    public var apiKey: String
    public fun getInitializedRouter(type: Router.Type): Router
}
