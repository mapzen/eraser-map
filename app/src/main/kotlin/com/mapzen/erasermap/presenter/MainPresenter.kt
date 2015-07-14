package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.view.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus

public interface MainPresenter {
    companion object {
        val DEFAULT_ZOOM: Float = 14f
        val ROUTING_ZOOM: Float = 17f
    }

    public var currentSearchTerm: String?
    public var mainViewController: MainViewController?
    public var bus: Bus?
    public var route: Route?
    public var routingEnabled: Boolean
    public var routeViewController: RouteViewController?

    public fun onSearchResultsAvailable(result: Result?)
    public fun onSearchResultSelected(position: Int)
    public fun onExpandSearchView()
    public fun onCollapseSearchView()
    public fun onRoutingCircleClick(reverse: Boolean)
    public fun onQuerySubmit()
    public fun onViewAllSearchResults()
    public fun onBackPressed()
    public fun onRestoreViewState()
    public fun onResumeRouting()
    public fun onLocationChanged(location: Location)
}
