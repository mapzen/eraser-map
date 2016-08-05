package com.mapzen.erasermap.model

import android.content.Context
import com.mapzen.android.routing.MapzenRouter

class TestRouterFactory : RouterFactory {

    companion object {
        var router: TestRouter? = null
    }

    override fun getRouter(context: Context): MapzenRouter {
        if (router == null) {
            router = TestRouter(context)
        }
        return router as MapzenRouter

    }
}
