package com.mapzen.erasermap.presenter

import android.location.Location
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.model.RoutePreviewEvent
import com.mapzen.erasermap.view.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.BuildConfig
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import java.util.ArrayList

public class MainPresenterImpl(val mapzenLocation: MapzenLocation) : MainPresenter {
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
    private var initialized = false

    enum class ViewState {
        DEFAULT,
        SEARCH,
        SEARCH_RESULTS,
        ROUTE_PREVIEW,
        ROUTING,
        ROUTE_DIRECTION_LIST
    }

    var viewState: ViewState = ViewState.DEFAULT

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
        var features = ArrayList<Feature>()
        this.searchResults = searchResults
        if(searchResults?.getFeatures()?.isEmpty() as Boolean) {
            features.add(currentFeature)
            mainViewController?.showReverseGeocodeFeature(features)
            searchResults?.setFeatures(features)
        } else {
            features.add(searchResults?.getFeatures()?.get(0))
            searchResults?.setFeatures(features)
            mainViewController?.showReverseGeocodeFeature(features)
        }
    }

    override fun onRestoreViewState() {
        if (destination != null) {
            if(routingEnabled) {
                generateRoutingMode()
            } else {
                generateRoutePreview()
            }
        } else {
            if (searchResults != null) {
                mainViewController?.showSearchResults(searchResults?.getFeatures())
            }
        }

        if (viewState == ViewState.ROUTE_DIRECTION_LIST) {
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

    @Subscribe public fun onRoutePreviewEvent(event: RoutePreviewEvent) {
        viewState = ViewState.ROUTE_PREVIEW
        destination = event.destination;
        mainViewController?.collapseSearchView()
        generateRoutePreview()
    }

    override fun onBackPressed() {
        when (viewState) {
            ViewState.DEFAULT -> onBackPressedStateDefault()
            ViewState.SEARCH -> onBackPressedStateSearch()
            ViewState.SEARCH_RESULTS -> onBackPressedStateSearchResults()
            ViewState.ROUTE_PREVIEW -> onBackPressedStateRoutePreview()
            ViewState.ROUTING -> onBackPressedStateRouting()
            ViewState.ROUTE_DIRECTION_LIST -> onBackPressedStateRouteDirectionList()
        }
    }

    private fun onBackPressedStateDefault() {
        mainViewController?.shutDown()
    }

    private fun onBackPressedStateSearch() {
        viewState = ViewState.DEFAULT
        mainViewController?.collapseSearchView()
    }

    private fun onBackPressedStateSearchResults() {
        viewState = ViewState.SEARCH
        mainViewController?.hideSearchResults()
    }

    private fun onBackPressedStateRoutePreview() {
        viewState = ViewState.SEARCH_RESULTS
        mainViewController?.hideRoutePreview()
    }

    private fun onBackPressedStateRouting() {
        viewState = ViewState.ROUTE_PREVIEW
        mainViewController?.hideRoutingMode()
    }

    private fun onBackPressedStateRouteDirectionList() {
        viewState = ViewState.ROUTING
        routeViewController?.collapseSlideLayout()
    }

    override fun onRoutingCircleClick(reverse: Boolean) {
        if (reverse) {
            mainViewController?.showDirectionList()
        } else {
            mapzenLocation.initRouteLocationUpdates {
                location: Location -> onLocationChanged(location)
                if (BuildConfig.DEBUG) {
                    System.out.println("onLocationChanged(Routing): " + location)
                }
            }
            generateRoutingMode()
            viewState = ViewState.ROUTING
        }
    }

    override fun onResumeRouting() {
        val location = mapzenLocation.getLastLocation()
        if (location is Location) {
            mainViewController?.centerMapOnLocation(location, MainPresenter.ROUTING_ZOOM)
        }
    }

    override fun onLocationChanged(location: Location) {
        if (routingEnabled) {
            routeViewController?.onLocationChanged(location)
            mainViewController?.centerMapOnLocation(location, MainPresenter.ROUTING_ZOOM)
            // TODO: Re-enable routing tilt on Tangram update
            // mainViewController?.setMapTilt(MainPresenter.ROUTING_TILT)
        }
    }

    override fun onSlidingPanelOpen() {
        viewState = ViewState.ROUTE_DIRECTION_LIST
        routeViewController?.showDirectionList()
    }

    override fun onSlidingPanelCollapse() {
        viewState = ViewState.ROUTING
        routeViewController?.hideDirectionList()
    }

    override fun onInstructionSelected(instruction: Instruction) {
        mainViewController?.centerMapOnLocation(instruction.location, MainPresenter.ROUTING_ZOOM)
        // TODO: Re-enable routing tilt on Tangram update
        // mainViewController?.setMapTilt(MainPresenter.ROUTING_TILT)
        mainViewController?.setMapRotation(Math.toRadians(instruction.bearing.toDouble()).toFloat())
    }

    override fun onCreate() {
        if (!initialized) {
            mapzenLocation.connect()
            val currentLocation = mapzenLocation.getLastLocation()
            if (currentLocation is Location) {
                mainViewController?.centerMapOnLocation(currentLocation, MainPresenter.DEFAULT_ZOOM)
            }
            initialized = true
        }
    }

    override fun onResume() {
        if (!isRouting() && !isRoutingDirectionList()) {
            mapzenLocation.connect()
            mapzenLocation.initLocationUpdates {
                location: Location -> onLocationChanged(location)
                if (BuildConfig.DEBUG) {
                    System.out.println("onLocationChanged: " + location)
                }
            }
        }
    }

    override fun onPause() {
        if (!isRouting() && !isRoutingDirectionList()) {
            mapzenLocation.disconnect()
        }
    }

    private fun isRouting(): Boolean {
        return viewState == ViewState.ROUTING
    }

    private fun isRoutingDirectionList(): Boolean {
        return viewState == ViewState.ROUTE_DIRECTION_LIST
    }

    override fun onFindMeButtonClick() {
        val currentLocation = mapzenLocation.getLastLocation()
        if (currentLocation is Location) {
            mainViewController?.centerMapOnLocation(currentLocation, MainPresenter.DEFAULT_ZOOM)
        }
    }

    override fun getPeliasLocationProvider(): PeliasLocationProvider {
        return mapzenLocation
    }

    private fun generateRoutePreview() {
        val location = mapzenLocation.getLastLocation()
        val feature = destination
        if (location is Location && feature is Feature) {
            mainViewController?.showRoutePreview(location, feature)
        }
    }

    private fun generateRoutingMode() {
        val feature = destination
        if (feature is Feature) {
            mainViewController?.showRoutingMode(feature)
        }
    }
}
