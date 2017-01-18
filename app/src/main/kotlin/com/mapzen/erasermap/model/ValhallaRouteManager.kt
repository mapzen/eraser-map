package com.mapzen.erasermap.model

import android.content.Context
import com.mapzen.android.routing.MapzenRouter
import com.mapzen.erasermap.BuildConfig
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import okhttp3.logging.HttpLoggingInterceptor

class ValhallaRouteManager(val settings: AppSettings,
        val routerFactory: RouterFactory, val context: Context) : RouteManager {
    override var apiKey: String = ""
    override var origin: ValhallaLocation? = null
    override var destination: Feature? = null
    override var type: Router.Type = Router.Type.DRIVING
    override var reverse: Boolean = false
    override var route: Route? = null
    override var bearing: Float? = null
    override var currentRequest: RouteCallback? = null

    override fun fetchRoute(callback: RouteCallback) {
        currentRequest = callback
        if (reverse) {
            fetchReverseRoute(callback)
        } else {
            fetchForwardRoute(callback)
        }
    }

    override fun toggleReverse() {
        this.reverse = !reverse
    }

    private fun fetchForwardRoute(callback: RouteCallback) {
        val location = origin
        val simpleFeature = SimpleFeature.fromFeature(destination)
        if (location is ValhallaLocation) {
            val start: DoubleArray = doubleArrayOf(location.latitude, location.longitude)
            val dest: DoubleArray = doubleArrayOf(simpleFeature.lat(), simpleFeature.lng())
            val units: MapzenRouter.DistanceUnits = settings.distanceUnits
            var name: String? = null

            if (!simpleFeature.isAddress) {
                name = simpleFeature.name()
            }

            val street = simpleFeature.name()
            val city = simpleFeature.localAdmin()
            val state = simpleFeature.region()
            val router = getInitializedRouter(type)

            if (location.hasBearing()) {
                router.setLocation(start, location.bearing)
            } else {
                router.setLocation(start)
            }

            router.setLocation(dest, name, street, city, state)
                    .setDistanceUnits(units)
                    .setCallback(callback)
                    .fetch()
        }
    }

    private fun fetchReverseRoute(callback: RouteCallback) {
        val location = origin
        val simpleFeature = SimpleFeature.fromFeature(destination)
        if (location is ValhallaLocation) {
            val start: DoubleArray = doubleArrayOf(simpleFeature.lat(), simpleFeature.lng())
            val dest: DoubleArray = doubleArrayOf(location.latitude, location.longitude)
            val units: MapzenRouter.DistanceUnits = settings.distanceUnits
            getInitializedRouter(type)
                    .setLocation(start)
                    .setLocation(dest)
                    .setDistanceUnits(units)
                    .setCallback(callback)
                    .fetch()
        }
    }

    private fun getInitializedRouter(type: Router.Type): MapzenRouter {
        val endpoint = BuildConfig.ROUTE_BASE_URL ?: null
        val logLevel = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else
            HttpLoggingInterceptor.Level.NONE
        val httpHandler: ValhallaHttpHandler?
        if  (endpoint != null) {
            httpHandler = ValhallaHttpHandler(endpoint, logLevel)
        } else {
            httpHandler = ValhallaHttpHandler(logLevel)
        }
        httpHandler.setApiKey(apiKey);

        val router = routerFactory.getRouter(context)
        router.router.setHttpHandler(httpHandler)

        when(type) {
            Router.Type.DRIVING -> return router.setDriving()
            Router.Type.WALKING -> return router.setWalking()
            Router.Type.BIKING -> return router.setBiking()
            Router.Type.MULTIMODAL -> return router.setMultimodal()
        }
    }
}
