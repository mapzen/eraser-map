package com.mapzen.erasermap.presenter

import com.mapzen.erasermap.model.RoutePreviewEvent
import com.mapzen.erasermap.view.ViewController
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

public class MainPresenterImpl() : MainPresenter {
    override var route: Route? = null;
    override var routingEnabled : Boolean = false
    override var viewController: ViewController? = null
    override var currentSearchTerm: String? = null
    override var bus: Bus? = null
        set(bus) {
            bus?.register(this)
        }

    private var searchResults: Result? = null
    private var destination: Feature? = null

    override fun onSearchResultsAvailable(searchResults: Result?) {
        this.searchResults = searchResults
        viewController?.showSearchResults(searchResults?.getFeatures())
        viewController?.hideProgress()
        val featureCount = searchResults?.getFeatures()?.size()
        if (featureCount != null && featureCount > 1) {
            viewController?.showActionViewAll()
        } else {
            viewController?.hideActionViewAll()
        }
    }

    override fun onRestoreViewState() {
        if (destination != null) {
            if(routingEnabled) {
                viewController?.showRoutingMode(destination!!)
            } else {
                viewController?.showRoutePreview(destination!!)
            }
        } else {
            if (searchResults != null) {
                viewController?.showSearchResults(searchResults?.getFeatures())
            }
        }
    }

    override fun onExpandSearchView() {
        viewController?.hideOverflowMenu()
    }

    override fun onCollapseSearchView() {
        searchResults = null;
        viewController?.hideSearchResults()
        viewController?.showOverflowMenu()
        viewController?.hideActionViewAll()
    }

    override fun onQuerySubmit() {
        viewController?.showProgress()
    }

    override fun onSearchResultSelected(position: Int) {
        if (searchResults != null) {
            viewController?.centerOnCurrentFeature(searchResults?.getFeatures())
        }
    }

    override fun onViewAllSearchResults() {
        viewController?.showAllSearchResults(searchResults?.getFeatures())
    }

    [Subscribe] public fun onRoutePreviewEvent(event: RoutePreviewEvent) {
        destination = event.destination;
        viewController?.collapseSearchView()
        viewController?.showRoutePreview(event.destination)
    }

    override fun onBackPressed() {
        if (destination != null ) {
            if(routingEnabled == true) {
                viewController?.hideRoutingMode()
            } else {
                viewController?.hideRoutePreview()
                destination = null
            }
        } else {
            viewController?.shutDown()
        }
    }

    override fun onRoutingCircleClick(reverse: Boolean) {
        if(reverse) {
            viewController?.showDirectionList()
        } else {
            viewController?.showRoutingMode(destination!!)
        }
    }
}
