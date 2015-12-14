package com.mapzen.erasermap.model

import com.mapzen.valhalla.Router

public class TestRouterFactory : RouterFactory {
    val router = TestRouter()

    override fun getRouter(): Router {
        return router
    }
}
