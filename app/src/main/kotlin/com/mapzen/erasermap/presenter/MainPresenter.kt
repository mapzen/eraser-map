package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.controller.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.tangram.LngLat

public interface MainPresenter {
    companion object {
        val LONG_MANEUVER_ZOOM: Float = 15f
        val DEFAULT_ZOOM: Float = 16f
        val ROUTING_ZOOM: Float = 17f
        val ROUTING_TILT: Float = 0.96f // 55°
    }

    public var mainViewController: MainViewController?
    public var routeViewController: RouteViewController?
    public var currentSearchTerm: String?
    public var currentFeature: Feature?
    public var routingEnabled: Boolean
    public var resultListVisible: Boolean
    public var reverseGeo: Boolean
    public var reverseGeoLngLat: LngLat?

    public fun onSearchResultsAvailable(result: Result?)
    public fun onReverseGeocodeResultsAvailable(searchResults: Result?)
    public fun onPlaceSearchResultsAvailable(searchResults: Result?)
    public fun onSearchResultSelected(position: Int)
    public fun onSearchResultTapped(position: Int)
    public fun onExpandSearchView()
    public fun onCollapseSearchView()
    public fun onClickViewList()
    public fun onClickStartNavigation()
    public fun onQuerySubmit()
    public fun onViewAllSearchResults()
    public fun updateLocation()
    public fun onBackPressed()
    public fun onRestoreViewState()
    public fun onResume()
    public fun onFindMeButtonClick()
    public fun onMuteClick()
    public fun onCompassClick()
    public fun getPeliasLocationProvider(): PeliasLocationProvider
    public fun onReroute(location: ValhallaLocation)
    public fun onMapMotionEvent(): Boolean
    public fun onReverseGeoRequested(screenX: Float?, screenY: Float?): Boolean
    public fun onPlaceSearchRequested(gid: String): Boolean
    public fun onExitNavigation()
    public fun configureMapzenMap()
    public fun onIntentQueryReceived(query: String?)
}
