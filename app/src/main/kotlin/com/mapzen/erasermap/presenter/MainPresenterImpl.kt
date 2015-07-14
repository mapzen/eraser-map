package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.model.RoutePreviewEvent
import com.mapzen.erasermap.view.MainActivity
import com.mapzen.erasermap.view.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

public class MainPresenterImpl() : MainPresenter {
    override var route: Route? = null;
    override var routingEnabled : Boolean = false
    override var mainViewController: MainViewController? = null
    override var routeViewController: RouteViewController? = null
    override var currentSearchTerm: String? = null
    override var bus: Bus? = null
        set(bus) {
            bus?.register(this)
        }

    private var searchResults: Result? = null
    private var destination: Feature? = null

    override fun onSearchResultsAvailable(searchResults: Result?) {
        this.searchResults = searchResults
        mainViewController?.showSearchResults(searchResults?.getFeatures())
        mainViewController?.hideProgress()
        val featureCount = searchResults?.getFeatures()?.size()
        if (featureCount != null && featureCount > 1) {
            mainViewController?.showActionViewAll()
        } else {
            mainViewController?.hideActionViewAll()
        }
    }

    override fun onRestoreViewState() {
        if (destination != null) {
            if(routingEnabled) {
                mainViewController?.showRoutingMode(destination!!)
            } else {
                mainViewController?.showRoutePreview(destination!!)
            }
        } else {
            if (searchResults != null) {
                mainViewController?.showSearchResults(searchResults?.getFeatures())
            }
        }
    }

    override fun onExpandSearchView() {
        mainViewController?.hideOverflowMenu()
    }

    override fun onCollapseSearchView() {
        searchResults = null;
        mainViewController?.hideSearchResults()
        mainViewController?.showOverflowMenu()
        mainViewController?.hideActionViewAll()
    }

    override fun onQuerySubmit() {
        mainViewController?.showProgress()
    }

    override fun onSearchResultSelected(position: Int) {
        if (searchResults != null) {
            mainViewController?.centerOnCurrentFeature(searchResults?.getFeatures())
        }
    }

    override fun onViewAllSearchResults() {
        mainViewController?.showAllSearchResults(searchResults?.getFeatures())
    }

    [Subscribe] public fun onRoutePreviewEvent(event: RoutePreviewEvent) {
        destination = event.destination;
        mainViewController?.collapseSearchView()
        mainViewController?.showRoutePreview(event.destination)
    }

    override fun onBackPressed() {
        if (destination != null ) {
            if(routingEnabled == true) {
                mainViewController?.hideRoutingMode()
            } else {
                mainViewController?.hideRoutePreview()
                destination = null
            }
        } else {
            mainViewController?.shutDown()
        }
    }

    override fun onRoutingCircleClick(reverse: Boolean) {
        if(reverse) {
            mainViewController?.showDirectionList()
        } else {
            mainViewController?.showRoutingMode(destination!!)
        }
    }

    override fun onResumeRouting() {
        mainViewController?.centerMapOnCurrentLocation(MainPresenter.ROUTING_ZOOM)
    }

    override fun onLocationChanged(location: Location) {
        if (routingEnabled) {
            routeViewController?.onLocationChanged(location)
            mainViewController?.centerMapOnLocation(location, MainPresenter.ROUTING_ZOOM)
        }
    }
}
