package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.erasermap.BuildConfig
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import com.mapzen.valhalla.ValhallaRouter
import retrofit.RestAdapter

public class ValhallaRouteManager(val settings: AppSettings,
        val routerFactory: RouterFactory) : RouteManager {
    override var apiKey: String = ""
    override var origin: Location? = null
    override var destination: Feature? = null
    override var type: Router.Type = Router.Type.DRIVING
    override var reverse: Boolean = false
    override var route: Route? = null
    override var bearing: Float? = null

    override fun fetchRoute(callback: RouteCallback) {
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
        if (location is Location) {
            val start: DoubleArray = doubleArrayOf(location.latitude, location.longitude)
            val dest: DoubleArray = doubleArrayOf(simpleFeature.lat(), simpleFeature.lng())
            val units: Router.DistanceUnits = settings.distanceUnits
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
        if (location is Location) {
            val start: DoubleArray = doubleArrayOf(simpleFeature.lat(), simpleFeature.lng())
            val dest: DoubleArray = doubleArrayOf(location.latitude, location.longitude)
            val units: Router.DistanceUnits = settings.distanceUnits
            getInitializedRouter(type)
                    .setLocation(start)
                    .setLocation(dest)
                    .setDistanceUnits(units)
                    .setCallback(callback)
                    .fetch()
        }
    }

    private fun getInitializedRouter(type: Router.Type): Router {
        val endpoint = BuildConfig.ROUTE_BASE_URL ?: ValhallaRouter.DEFAULT_URL
        val logLevel = if (BuildConfig.DEBUG) RestAdapter.LogLevel.FULL else
            RestAdapter.LogLevel.NONE
        val router = routerFactory.getRouter()
                .setApiKey(apiKey)
                .setEndpoint(endpoint)
                .setLogLevel(logLevel)
                .setDntEnabled(true)
        when(type) {
            Router.Type.DRIVING -> return router.setDriving()
            Router.Type.WALKING -> return router.setWalking()
            Router.Type.BIKING -> return router.setBiking()
        }
    }
}
