package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.model.RoutePreviewEvent
import com.mapzen.erasermap.view.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import java.util.ArrayList

public class MainPresenterImpl() : MainPresenter {
    override var currentFeature: Feature? = null;
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

    private enum class ViewState {
        DEFAULT,
        SEARCH,
        SEARCH_RESULTS,
        ROUTING_PREVIEW,
        ROUTING,
        ROUTING_DIRECTION_LIST
    }

    private var viewState: ViewState = ViewState.DEFAULT

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

    override fun onReverseGeocodeResultsAvailable(searchResults: Result?) {
        this.searchResults = searchResults
        if(searchResults?.getFeatures()?.isEmpty() as Boolean) {
            var features = ArrayList<Feature>()
            features.add(currentFeature)
            mainViewController?.showReverseGeocodeFeature(features)
        } else {
            mainViewController?.showReverseGeocodeFeature(searchResults?.getFeatures())
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

        if (viewState == ViewState.ROUTING_DIRECTION_LIST) {
            routeViewController?.showDirectionList()
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
            if(searchResults == null) {
                mainViewController?.shutDown()
            } else {
                mainViewController?.hideSearchResults()
                searchResults = null
            }
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

    override fun onSlidingPanelOpen() {
        viewState = ViewState.ROUTING_DIRECTION_LIST
        routeViewController?.showDirectionList()
    }

    override fun onSlidingPanelCollapse() {
        viewState = ViewState.ROUTING
        routeViewController?.hideDirectionList()
    }
}
