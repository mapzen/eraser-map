package com.mapzen.erasermap.model

import android.content.Context
import android.location.Location
import com.mapzen.pelias.SavedSearch
import com.mapzen.valhalla.Router
import java.io.File

interface AppSettings {
    var distanceUnits: Router.DistanceUnits
    var isMockLocationEnabled: Boolean
    var mockLocation: Location
    var isMockRouteEnabled: Boolean
    var mockRoute: File
    var isTileDebugEnabled: Boolean
    var isLabelDebugEnabled: Boolean
    var isTangramInfosDebugEnabled: Boolean
    var isCacheSearchResultsEnabled: Boolean

    fun initTangramDebugFlags()
    fun initSearchResultVersion(context: Context, savedSearch: SavedSearch)
}
