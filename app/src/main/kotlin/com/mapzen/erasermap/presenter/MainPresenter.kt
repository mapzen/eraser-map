package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.view.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus

public interface MainPresenter {
    companion object {
        val DEFAULT_ZOOM: Float = 14f
        val ROUTING_ZOOM: Float = 17f

        val DEFAULT_TILT: Float = 0f
        val ROUTING_TILT: Float = 0.785398163f // 45°
    }

    public var currentSearchTerm: String?
    public var currentFeature: Feature?
    public var mainViewController: MainViewController?
    public var bus: Bus?
    public var route: Route?
    public var routingEnabled: Boolean
    public var routeViewController: RouteViewController?

    public fun onSearchResultsAvailable(result: Result?)
    public fun onReverseGeocodeResultsAvailable(searchResults: Result?)
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
    public fun onSlidingPanelOpen()
    public fun onSlidingPanelCollapse()
    public fun onInstructionSelected(instruction: Instruction)
    public fun onPause()
}
