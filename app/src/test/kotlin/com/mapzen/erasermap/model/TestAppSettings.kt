package com.mapzen.erasermap.model

import android.content.Context
import android.location.Location
import com.mapzen.android.MapzenMap
import com.mapzen.erasermap.dummy.TestHelper
import com.mapzen.pelias.SavedSearch
import com.mapzen.tangram.MapController
import com.mapzen.valhalla.Router
import java.io.File

public class TestAppSettings : AppSettings {
    override var distanceUnits: Router.DistanceUnits = Router.DistanceUnits.MILES
    override var isMockLocationEnabled: Boolean = false
    override var mockLocation: Location = TestHelper.getTestLocation()
    override var isMockRouteEnabled: Boolean = false
    override var mockRoute: File = File("lost.gpx")
    override var isTileDebugEnabled: Boolean = false
    override var isLabelDebugEnabled: Boolean = false
    override var isTangramInfosDebugEnabled: Boolean = false
    override var isCacheSearchResultsEnabled: Boolean = true
    override var mapzenMap: MapzenMap? = null

    override fun initTangramDebugFlags() {
    }

    override fun initSearchResultVersion(context: Context, savedSearch: SavedSearch) {
    }
}

