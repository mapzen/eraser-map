package com.mapzen.erasermap.model

import com.mapzen.valhalla.Router
import com.mapzen.valhalla.ValhallaRouter

public class ValhallaRouterFactory : RouterFactory {
    override fun getRouter(): Router {
        return ValhallaRouter()
    }
}
