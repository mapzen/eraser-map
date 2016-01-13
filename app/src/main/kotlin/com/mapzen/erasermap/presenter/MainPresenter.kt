package com.mapzen.erasermap.presenter

import android.location.Location
import android.view.MotionEvent
import com.mapzen.erasermap.view.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result

public interface MainPresenter {
    companion object {
        val DEFAULT_ZOOM: Float = 16f
        val ROUTING_ZOOM: Float = 18f
        val ROUTING_TILT: Float = 1.0472f // 60°
    }

    public var mainViewController: MainViewController?
    public var routeViewController: RouteViewController?
    public var currentSearchTerm: String?
    public var currentFeature: Feature?
    public var routingEnabled: Boolean

    public fun onSearchResultsAvailable(result: Result?)
    public fun onReverseGeocodeResultsAvailable(searchResults: Result?)
    public fun onSearchResultSelected(position: Int)
    public fun onExpandSearchView()
    public fun onCollapseSearchView()
    public fun onClickViewList()
    public fun onClickStartNavigation()
    public fun onQuerySubmit()
    public fun onViewAllSearchResults()
    public fun onBackPressed()
    public fun onRestoreViewState()
    public fun onSlidingPanelOpen()
    public fun onSlidingPanelCollapse()
    public fun onCreate()
    public fun onResume()
    public fun onPause()
    public fun onFindMeButtonClick()
    public fun onCompassClick()
    public fun getPeliasLocationProvider(): PeliasLocationProvider
    public fun onReroute(location: Location)
    public fun onMapMotionEvent(): Boolean
    public fun onLongPressMap(screenX: Float, screenY: Float): Boolean
    open fun onExitNavigation()
}
