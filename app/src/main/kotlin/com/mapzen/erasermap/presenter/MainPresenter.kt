package com.mapzen.erasermap.presenter

import com.mapzen.erasermap.view.ViewController
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus

public interface MainPresenter {
    public var currentSearchTerm: String?
    public var viewController: ViewController?
    public var bus: Bus?
    public var route: Route?
    public var routingEnabled: Boolean

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
}
