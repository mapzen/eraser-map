package com.mapzen.erasermap.model

import com.mapzen.erasermap.BuildConfig
import com.mapzen.valhalla.Router
import com.mapzen.valhalla.ValhallaRouter

public class ValhallaRouterFactory : RouterFactory {
    override var apiKey: String = BuildConfig.VALHALLA_API_KEY

    override fun getInitializedRouter(type: Router.Type): Router {
        val router = ValhallaRouter().setApiKey(apiKey)
        when(type) {
            Router.Type.DRIVING -> return router.setDriving()
            Router.Type.WALKING -> return router.setWalking()
            Router.Type.BIKING -> return router.setBiking()
        }
    }
}
