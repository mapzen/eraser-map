package com.mapzen.erasermap.presenter

import com.mapzen.erasermap.controller.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.tangram.LngLat
import com.mapzen.valhalla.RouteCallback

interface MainPresenter {
    companion object {
        val LONG_MANEUVER_ZOOM: Float = 15f
        val DEFAULT_ZOOM: Float = 16f
        val ROUTING_ZOOM: Float = 17f
        val ROUTING_TILT: Float = 0.96f // 55°
    }

    var mainViewController: MainViewController?
    var routeViewController: RouteViewController?
    var currentSearchTerm: String?
    var currentFeature: Feature?
    var routingEnabled: Boolean
    var resultListVisible: Boolean
    var reverseGeo: Boolean
    var reverseGeoLngLat: LngLat?

    fun onSearchResultsAvailable(result: Result?)
    fun onReverseGeocodeResultsAvailable(searchResults: Result?)
    fun onPlaceSearchResultsAvailable(searchResults: Result?)
    fun onSearchResultSelected(position: Int)
    fun onSearchResultTapped(position: Int)
    fun onExpandSearchView()
    fun onCollapseSearchView()
    fun onClickViewList()
    fun onClickStartNavigation()
    fun onQuerySubmit()
    fun onViewAllSearchResultsList()
    fun updateLocation()
    fun onBackPressed()
    fun onRestoreViewState()
    fun onRestoreOptionsMenu()
    fun onRestoreMapState()
    fun onResume()
    fun onMuteClick()
    fun onCompassClick()
    fun getPeliasLocationProvider(): PeliasLocationProvider
    fun onReroute(location: ValhallaLocation)
    fun onMapMotionEvent(): Boolean
    fun onReverseGeoRequested(screenX: Float?, screenY: Float?): Boolean
    fun onPlaceSearchRequested(gid: String): Boolean
    fun onExitNavigation()
    fun configureMapzenMap()
    fun onIntentQueryReceived(query: String?)
    fun onRouteRequest(callback: RouteCallback)
    fun generateRawFeature(): Feature
    fun onFeaturePicked(properties: Map<String, String>, poiPoint: FloatArray)
    fun checkPermissionAndEnableLocation()
}
