package com.mapzen.erasermap.presenter

import android.location.Location
import android.util.Log
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.model.LocationChangeEvent
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.model.RouteEvent
import com.mapzen.erasermap.model.RouteManager
import com.mapzen.erasermap.model.RoutePreviewEvent
import com.mapzen.erasermap.view.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import java.util.ArrayList

public open class MainPresenterImpl(val mapzenLocation: MapzenLocation, val bus: Bus,
        val routeManager: RouteManager, val settings: AppSettings, val vsm: ViewStateManager)
        : MainPresenter, RouteCallback {

    override var currentFeature: Feature? = null
    override var routingEnabled : Boolean = false
    override var mainViewController: MainViewController? = null
    override var routeViewController: RouteViewController? = null
    override var currentSearchTerm: String? = null

    private var searchResults: Result? = null
    private var destination: Feature? = null
    private var initialized = false

    init {
        bus.register(this)
    }

    override fun onSearchResultsAvailable(searchResults: Result?) {
        vsm.viewState = ViewStateManager.ViewState.SEARCH_RESULTS
        this.searchResults = searchResults
        mainViewController?.showSearchResults(searchResults?.getFeatures())
        mainViewController?.hideProgress()
        val featureCount = searchResults?.features?.size
        if (featureCount != null && featureCount > 1) {
            mainViewController?.showActionViewAll()
            mainViewController?.hideOverflowMenu()
        } else {
            mainViewController?.hideActionViewAll()
            mainViewController?.showOverflowMenu()
        }
    }

    override fun onReverseGeocodeResultsAvailable(searchResults: Result?) {
        vsm.viewState = ViewStateManager.ViewState.SEARCH_RESULTS
        var features = ArrayList<Feature>()
        this.searchResults = searchResults
        if(searchResults?.features?.isEmpty() as Boolean) {
            val current = currentFeature
            if (current is Feature) {
                features.add(current)
            }
            mainViewController?.showReverseGeocodeFeature(features)
            searchResults?.setFeatures(features)
        } else {
            val current = searchResults?.features?.get(0)
            if (current is Feature) {
                features.add(current)
            }
            searchResults?.features = features
            mainViewController?.showReverseGeocodeFeature(features)
        }
    }

    override fun onRestoreViewState() {
        if (destination != null) {
            if(routingEnabled) {
                resumeRoutingMode()
            } else {
                generateRoutePreview()
            }
        } else {
            if (searchResults != null) {
                mainViewController?.showSearchResults(searchResults?.getFeatures())
            }
        }

        if (vsm.viewState == ViewStateManager.ViewState.ROUTE_DIRECTION_LIST) {
            routeViewController?.showDirectionList()
        }
    }

    override fun onExpandSearchView() {
        vsm.viewState = ViewStateManager.ViewState.SEARCH
    }

    override fun onCollapseSearchView() {
        if (vsm.viewState != ViewStateManager.ViewState.ROUTE_PREVIEW) {
            vsm.viewState = ViewStateManager.ViewState.DEFAULT
        }

        searchResults = null
        mainViewController?.hideSearchResults()
        mainViewController?.showOverflowMenu()
        mainViewController?.hideActionViewAll()
        mainViewController?.clearQuery()
    }

    override fun onQuerySubmit() {
        mainViewController?.showProgress()
    }

    override fun onSearchResultSelected(position: Int) {
        if (searchResults != null) {
            mainViewController?.addSearchResultsToMap(searchResults?.getFeatures(), position)
            mainViewController?.centerOnCurrentFeature(searchResults?.features)
        }
    }

    override fun onSearchResultTapped(position: Int) {
        if (searchResults != null) {
            mainViewController?.addSearchResultsToMap(searchResults?.getFeatures(), position)
            mainViewController?.centerOnTappedFeature(searchResults?.features, position)
        }
    }

    override fun onViewAllSearchResults() {
        mainViewController?.showAllSearchResults(searchResults?.features)
    }

    @Subscribe public fun onRoutePreviewEvent(event: RoutePreviewEvent) {
        vsm.viewState = ViewStateManager.ViewState.ROUTE_PREVIEW
        destination = event.destination
        mainViewController?.collapseSearchView()
        mainViewController?.hideSearchResults()
        generateRoutePreview()
    }

    override fun onBackPressed() {
        when (vsm.viewState) {
            ViewStateManager.ViewState.DEFAULT -> onBackPressedStateDefault()
            ViewStateManager.ViewState.SEARCH -> onBackPressedStateSearch()
            ViewStateManager.ViewState.SEARCH_RESULTS -> onBackPressedStateSearchResults()
            ViewStateManager.ViewState.ROUTE_PREVIEW -> onBackPressedStateRoutePreview()
            ViewStateManager.ViewState.ROUTING -> onBackPressedStateRouting()
            ViewStateManager.ViewState.ROUTE_DIRECTION_LIST -> onBackPressedStateRouteDirectionList()
        }
    }

    private fun onBackPressedStateDefault() {
        mainViewController?.shutDown()
    }

    private fun onBackPressedStateSearch() {
        vsm.viewState = ViewStateManager.ViewState.DEFAULT
        mainViewController?.collapseSearchView()
    }

    private fun onBackPressedStateSearchResults() {
        vsm.viewState = ViewStateManager.ViewState.DEFAULT
        mainViewController?.collapseSearchView()
        mainViewController?.hideReverseGeolocateResult()
        mainViewController?.hideSearchResults()
    }

    private fun onBackPressedStateRoutePreview() {
        vsm.viewState = ViewStateManager.ViewState.SEARCH_RESULTS
        mainViewController?.hideRoutePreview()
        mainViewController?.clearRoute()
    }

    private fun onBackPressedStateRouting() {
        vsm.viewState = ViewStateManager.ViewState.ROUTE_PREVIEW
        mainViewController?.hideRoutingMode()
    }

    private fun onBackPressedStateRouteDirectionList() {
        vsm.viewState = ViewStateManager.ViewState.ROUTING
        routeViewController?.collapseSlideLayout()
    }

    override fun onClickViewList() {
        mainViewController?.showDirectionList()
    }

    override fun onClickStartNavigation() {
        bus.post(RouteEvent())
        generateRoutingMode()
        vsm.viewState = ViewStateManager.ViewState.ROUTING
    }

    @Subscribe public fun onLocationChangeEvent(event: LocationChangeEvent) {
        if (routingEnabled) {
            routeViewController?.onLocationChanged(event.location)
        }
    }

    override fun onSlidingPanelOpen() {
        vsm.viewState = ViewStateManager.ViewState.ROUTE_DIRECTION_LIST
        routeViewController?.showDirectionList()
    }

    override fun onSlidingPanelCollapse() {
        vsm.viewState = ViewStateManager.ViewState.ROUTING
        routeViewController?.hideDirectionList()
    }

    override fun onCreate() {
        val currentLocation = mapzenLocation.getLastLocation()
        if (currentLocation is Location) {
            if (!initialized) {
                // Show location puck and center map
                mainViewController?.centerMapOnLocation(currentLocation, MainPresenter.DEFAULT_ZOOM)
                initialized = true
            } else {
                // Just show location puck. Do not recenter map.
                mainViewController?.showCurrentLocation(currentLocation)
            }
        }
    }

    override fun onResume() {
        if (!isRouting() && !isRoutingDirectionList()) {
            mapzenLocation.startLocationUpdates()
        }
    }

    override fun onPause() {
        if (!isRouting() && !isRoutingDirectionList()) {
            mapzenLocation.stopLocationUpdates()
        }
    }

    private fun isRouting(): Boolean {
        return vsm.viewState == ViewStateManager.ViewState.ROUTING
    }

    private fun isRoutingDirectionList(): Boolean {
        return vsm.viewState == ViewStateManager.ViewState.ROUTE_DIRECTION_LIST
    }

    override fun onFindMeButtonClick() {
        val currentLocation = mapzenLocation.getLastLocation()
        if (currentLocation is Location) {
            mainViewController?.centerMapOnLocation(currentLocation, MainPresenter.DEFAULT_ZOOM)
            mainViewController?.setMapTilt(0f)
            mainViewController?.setMapRotation(0f)
        }
    }

    override fun onCompassClick() {
        mainViewController?.setMapRotation(0f)
    }

    override fun getPeliasLocationProvider(): PeliasLocationProvider {
        return mapzenLocation
    }

    override fun onReroute(location: Location) {
        mainViewController?.showProgress()
        fetchNewRoute(location)
    }

    private fun fetchNewRoute(location: Location) {
        routeManager.origin = location
        routeManager.destination = destination
        routeManager.reverse = false
        if (location.hasBearing()) {
            routeManager.bearing = location.bearing
        } else {
            routeManager.bearing = null
        }
        routeManager.fetchRoute(this)
    }

    override fun failure(statusCode: Int) {
        mainViewController?.hideProgress()
        Log.e("MainPresenterImpl", "Error fetching new route: " + statusCode)
    }

    override fun success(route: Route) {
        mainViewController?.hideProgress()
        routeManager.route = route
        generateRoutingMode()
        mainViewController?.drawRoute(route)
    }

    private fun generateRoutePreview() {
        val location = mapzenLocation.getLastLocation()
        val feature = destination
        if (location is Location && feature is Feature) {
            mainViewController?.showRoutePreview(location, feature)
        }
    }

    private fun generateRoutingMode() {
        routingEnabled = true
        val feature = destination
        if (feature is Feature) {
            mainViewController?.startRoutingMode(feature)
        }
    }

    private fun resumeRoutingMode() {
        routingEnabled = true
        val feature = destination
        if (feature is Feature) {
            mainViewController?.resumeRoutingMode(feature)
        }
    }

    override fun onExitNavigation() {
        vsm.viewState = ViewStateManager.ViewState.SEARCH_RESULTS
        routingEnabled = false;
        routeManager?.reverse = false
        onFindMeButtonClick()
    }

    override fun onMapMotionEvent(): Boolean {
        mainViewController?.rotateCompass()
        return false
    }

    override fun onLongPressMap(screenX: Float, screenY: Float): Boolean {
        if (vsm.viewState == ViewStateManager.ViewState.ROUTE_PREVIEW
                || vsm.viewState == ViewStateManager.ViewState.ROUTING
                || vsm.viewState == ViewStateManager.ViewState.ROUTE_DIRECTION_LIST) {
            return false
        }

        mainViewController?.reverseGeolocate(screenX, screenY)
        return true
    }
}
