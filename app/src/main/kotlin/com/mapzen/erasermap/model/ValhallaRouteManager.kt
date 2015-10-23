package com.mapzen.erasermap.model

import android.location.Location
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import com.mapzen.valhalla.ValhallaRouter

public class ValhallaRouteManager(val settings: AppSettings) : RouteManager {
    override var apiKey: String = ""
    override var origin: Location? = null
    override var destination: Feature? = null
    override var type: Router.Type = Router.Type.DRIVING
    override var reverse: Boolean = false
    override var route: Route? = null

    override fun fetchRoute(callback: RouteCallback) {
        if (reverse) {
            fetchReverseRoute(callback)
        } else {
            fetchForwardRoute(callback)
        }
    }

    private fun fetchForwardRoute(callback: RouteCallback) {
        val location = origin
        val simpleFeature = SimpleFeature.fromFeature(destination)
        if (location is Location) {
            val start: DoubleArray = doubleArrayOf(location.latitude, location.longitude)
            val dest: DoubleArray = doubleArrayOf(simpleFeature.lat, simpleFeature.lon)
            val units: Router.DistanceUnits = settings.distanceUnits
            val name = destination?.properties?.name
            val street = simpleFeature.title
            val city = simpleFeature.city
            val state = simpleFeature.admin
            getInitializedRouter(type)
                    .setLocation(start)
                    .setLocation(dest, name, street, city, state)
                    .setDistanceUnits(units)
                    .setCallback(callback)
                    .fetch()
        }
    }

    private fun fetchReverseRoute(callback: RouteCallback) {
        val location = origin
        val simpleFeature = SimpleFeature.fromFeature(destination)
        if (location is Location) {
            val start: DoubleArray = doubleArrayOf(simpleFeature.lat, simpleFeature.lon)
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
        val router = ValhallaRouter().setApiKey(apiKey)
        when(type) {
            Router.Type.DRIVING -> return router.setDriving()
            Router.Type.WALKING -> return router.setWalking()
            Router.Type.BIKING -> return router.setBiking()
        }
    }
}
