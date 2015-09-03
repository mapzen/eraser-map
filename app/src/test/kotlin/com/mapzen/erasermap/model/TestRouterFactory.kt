package com.mapzen.erasermap.model

import com.mapzen.erasermap.BuildConfig
import com.mapzen.valhalla.JSON
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import java.util.ArrayList

public class TestRouterFactory : RouterFactory {
    public var locations: ArrayList<DoubleArray> = ArrayList()
    public var isFetching: Boolean = false

    override var apiKey: String = BuildConfig.VALHALLA_API_KEY

    override fun getInitializedRouter(type: Router.Type): Router {
        return TestRouter()
    }

    public fun reset() {
        locations.clear()
    }

    public inner class TestRouter : Router {
        override fun clearLocations(): Router {
            locations.clear()
            return this
        }

        override fun fetch() {
            isFetching = true
        }

        override fun getEndpoint(): String {
            return ""
        }

        override fun getJSONRequest(): JSON {
            return JSON()
        }

        override fun setApiKey(key: String): Router {
            return this
        }

        override fun setBiking(): Router {
            return this
        }

        override fun setCallback(callback: RouteCallback): Router {
            return this
        }

        override fun setDriving(): Router {
            return this
        }

        override fun setEndpoint(url: String): Router {
            return this
        }

        override fun setLocation(point: DoubleArray): Router {
            locations.add(point)
            return this
        }

        override fun setWalking(): Router {
            return this
        }
    }
}
