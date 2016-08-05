package com.mapzen.erasermap.model

import android.content.Context
import android.location.Location
import com.mapzen.android.graphics.MapzenMap
import com.mapzen.android.routing.MapzenRouter
import com.mapzen.pelias.SavedSearch
import java.io.File

interface AppSettings {
    var distanceUnits: MapzenRouter.DistanceUnits
    var isMockLocationEnabled: Boolean
    var mockLocation: Location
    var isMockRouteEnabled: Boolean
    var mockRoute: File?
    var isTileDebugEnabled: Boolean
    var isLabelDebugEnabled: Boolean
    var isTangramInfosDebugEnabled: Boolean
    var isCacheSearchResultsEnabled: Boolean
    var mapzenMap: MapzenMap?

    fun initTangramDebugFlags()
    fun initSearchResultVersion(context: Context, savedSearch: SavedSearch)
}
