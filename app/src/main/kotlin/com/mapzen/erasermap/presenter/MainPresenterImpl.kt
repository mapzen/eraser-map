package com.mapzen.erasermap.presenter

import android.location.Location
import android.util.Log
import com.mapzen.erasermap.controller.MainViewController
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.model.IntentQuery
import com.mapzen.erasermap.model.IntentQueryParser
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.model.RouteManager
import com.mapzen.erasermap.model.event.LocationChangeEvent
import com.mapzen.erasermap.model.event.RouteCancelEvent
import com.mapzen.erasermap.model.event.RouteEvent
import com.mapzen.erasermap.model.event.RoutePreviewEvent
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.DEFAULT
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_DIRECTION_LIST
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_PREVIEW
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTE_PREVIEW_LIST
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.ROUTING
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.SEARCH
import com.mapzen.erasermap.presenter.ViewStateManager.ViewState.SEARCH_RESULTS
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.tangram.LngLat
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import java.util.ArrayList

open class MainPresenterImpl(val mapzenLocation: MapzenLocation, val bus: Bus,
        val routeManager: RouteManager, val settings: AppSettings, val vsm: ViewStateManager,
        val intentQueryParser: IntentQueryParser)
        : MainPresenter, RouteCallback {

    companion object {
        private val TAG = MainPresenterImpl::class.java.simpleName
    }

    override var currentFeature: Feature? = null
    override var routingEnabled : Boolean = false
    override var mainViewController: MainViewController? = null
    override var routeViewController: RouteViewController? = null
    override var currentSearchTerm: String? = null
    override var resultListVisible = false
    override var reverseGeo = false
    override var reverseGeoLngLat: LngLat? = null

    private var searchResults: Result? = null
    private var destination: Feature? = null
    private var initialized = false
    private var restoreReverseGeoOnBack = false


    /**
     * We will migrate to Retrofit2 where we will have ability to cancel requests. Before then,
     * we want to ignore all {@link RouteManager#fetchRoute} requests until we receive a
     * result for existing request. Flag indicates we have already issued request for new route and
     * are awaiting response
     */
    private var waitingForRoute = false

    init {
        bus.register(this)
    }

    override fun onSearchResultsAvailable(result: Result?) {
        vsm.viewState = ViewStateManager.ViewState.SEARCH_RESULTS
        reverseGeo = false
        this.searchResults = result
        mainViewController?.showSearchResults(result?.features)
        mainViewController?.hideProgress()
        val featureCount = result?.features?.size
        if (featureCount != null && featureCount > 1) {
            mainViewController?.showActionViewAll()
        } else {
            mainViewController?.hideActionViewAll()
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

    override fun onPlaceSearchResultsAvailable(searchResults: Result?) {
        vsm.viewState = ViewStateManager.ViewState.SEARCH_RESULTS
        var features = ArrayList<Feature>()
        this.searchResults = searchResults
        if(searchResults?.features?.isEmpty() as Boolean) {
            mainViewController?.emptyPlaceSearch()
        } else {
            val current = searchResults?.features?.get(0)
            if (current is Feature) {
                features.add(current)
                mainViewController?.overridePlaceFeature(features[0])
            }
            searchResults?.features = features
            mainViewController?.showPlaceSearchFeature(features)
        }
    }

    override fun onRestoreViewState() {
        when (vsm.viewState) {
            DEFAULT -> onRestoreViewStateDefault()
            SEARCH -> onRestoreViewStateSearch()
            SEARCH_RESULTS -> onRestoreViewStateSearchResults()
            ROUTE_PREVIEW -> onRestoreViewStateRoutePreview()
            ROUTE_PREVIEW_LIST -> onRestoreViewStateRoutePreviewList()
            ROUTING -> onRestoreViewStateRouting()
            ROUTE_DIRECTION_LIST -> onRestoreViewStateRouteDirectionList()
        }
   }

    private fun onRestoreViewStateDefault() {
        // Do nothing.
    }

    private fun onRestoreViewStateSearch() {
        // Do nothing.
    }

    private fun onRestoreViewStateSearchResults() {
        if (searchResults?.features != null) {
            if (!reverseGeo) {
                mainViewController?.showSearchResults(searchResults?.features)
            } else {
                mainViewController?.showReverseGeocodeFeature(searchResults?.features)
                mainViewController?.centerOnCurrentFeature(searchResults?.features)
            }
        }
    }

    private fun onRestoreViewStateRoutePreview() {
        generateRoutePreview()
    }

    private fun onRestoreViewStateRoutePreviewList() {
        generateRoutePreview()
        onClickViewList()
    }

    private fun onRestoreViewStateRouting() {
        resumeRoutingMode()
    }

    private fun onRestoreViewStateRouteDirectionList() {
        resumeRoutingMode()
        routeViewController?.showRouteDirectionList()
    }

    override fun onExpandSearchView() {
        vsm.viewState = ViewStateManager.ViewState.SEARCH
        mainViewController?.hideSettingsBtn()
    }

    override fun onCollapseSearchView() {
        if (vsm.viewState != ViewStateManager.ViewState.ROUTE_PREVIEW) {
            vsm.viewState = ViewStateManager.ViewState.DEFAULT
        }

        mainViewController?.hideSearchResults()
        mainViewController?.hideActionViewAll()
        if (vsm.viewState != ViewStateManager.ViewState.ROUTE_PREVIEW) {
            mainViewController?.clearQuery()
            mainViewController?.showSettingsBtn()
        }
    }

    override fun onQuerySubmit() {
        mainViewController?.showProgress()
    }

    override fun onSearchResultSelected(position: Int) {
        if (searchResults != null) {
            mainViewController?.addSearchResultsToMap(searchResults?.features, position)
            mainViewController?.centerOnCurrentFeature(searchResults?.features)
        }
    }

    override fun onSearchResultTapped(position: Int) {
        if (searchResults != null) {
            mainViewController?.addSearchResultsToMap(searchResults?.features, position)
            mainViewController?.centerOnFeature(searchResults?.features, position)
        }
    }

    override fun onViewAllSearchResults() {
        mainViewController?.showAllSearchResults(searchResults?.features)
    }

    @Subscribe public fun onRoutePreviewEvent(event: RoutePreviewEvent) {
        vsm.viewState = ViewStateManager.ViewState.ROUTE_PREVIEW
        if (reverseGeo) {
            restoreReverseGeoOnBack = true
        }
        reverseGeo = false
        destination = event.destination
        mainViewController?.collapseSearchView()
        mainViewController?.hideSearchResults()
        mainViewController?.hideReverseGeolocateResult()
        generateRoutePreview()
    }

    @Subscribe public fun onRouteCancelEvent(event: RouteCancelEvent) {
        mainViewController?.onBackPressed()
    }

    override fun updateLocation() {
        val location = mapzenLocation.getLastLocation()
        if (location != null) {
            routeViewController?.onLocationChanged(location)
        }
    }

    override fun onBackPressed() {
        if(vsm.viewState == SEARCH || vsm.viewState == SEARCH_RESULTS ) {
            currentSearchTerm = null
        }
        when (vsm.viewState) {
            DEFAULT -> onBackPressedStateDefault()
            SEARCH -> onBackPressedStateSearch()
            SEARCH_RESULTS -> onBackPressedStateSearchResults()
            ROUTE_PREVIEW -> onBackPressedStateRoutePreview()
            ROUTE_PREVIEW_LIST -> onBackPressedStateRoutePreviewList()
            ROUTING -> onBackPressedStateRouting()
            ROUTE_DIRECTION_LIST -> onBackPressedStateRouteDirectionList()
        }
        resultListVisible = false
    }

    private fun onBackPressedStateDefault() {
        mainViewController?.shutDown()
    }

    private fun onBackPressedStateSearch() {
        vsm.viewState = ViewStateManager.ViewState.DEFAULT
        searchResults = null
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
        if (restoreReverseGeoOnBack) {
            restoreReverseGeoOnBack = false
            reverseGeo = true
        }
        mainViewController?.hideRoutePreview()
        mainViewController?.clearRoute()
        if (searchResults != null) {
            if (reverseGeo) {
                mainViewController?.showReverseGeocodeFeature(searchResults?.features)
            } else {
                mainViewController?.showSearchResults(searchResults?.features)
                var numFeatures = 0
                numFeatures = (searchResults?.features?.size as Int)
                if(numFeatures > 1) {
                    mainViewController?.showActionViewAll()
                }
            }
        }
    }

    private fun onBackPressedStateRoutePreviewList() {
        vsm.viewState = ViewStateManager.ViewState.ROUTE_PREVIEW
        mainViewController?.hideDirectionsList()
    }

    private fun onBackPressedStateRouting() {
        vsm.viewState = ViewStateManager.ViewState.ROUTE_PREVIEW
        routingEnabled = false
        mainViewController?.hideRoutingMode()
        mainViewController?.stopSpeaker()
    }

    private fun onBackPressedStateRouteDirectionList() {
        vsm.viewState = ViewStateManager.ViewState.ROUTING
        routeViewController?.hideRouteDirectionList()
    }

    override fun onClickViewList() {
        vsm.viewState = ViewStateManager.ViewState.ROUTE_PREVIEW_LIST
        mainViewController?.showDirectionsList()
    }

    override fun onClickStartNavigation() {
        bus.post(RouteEvent())
        mainViewController?.resetMute() //must call before generateRoutingMode()
        generateRoutingMode()
        vsm.viewState = ViewStateManager.ViewState.ROUTING
        routeViewController?.hideResumeButton()
    }

    @Subscribe public fun onLocationChangeEvent(event: LocationChangeEvent) {
        if (routingEnabled) {
            routeViewController?.onLocationChanged(event.location)
        }
        //TODO
//        else {
//            mainViewController?.showCurrentLocation(event.location)
//        }
    }

    override fun onResume() {
        if (!isRouting() && !isRoutingDirectionList()) {
            mapzenLocation.startLocationUpdates()
            mainViewController?.checkPermissionAndEnableLocation()
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
            mainViewController?.centerMapOnLocation(LngLat(currentLocation.longitude,
                    currentLocation.latitude), MainPresenter.DEFAULT_ZOOM)
            mainViewController?.setMapTilt(0f)
            mainViewController?.setMapRotation(0f)
        }
    }

    override fun onMuteClick() {
        mainViewController?.toggleMute()
    }

    override fun onCompassClick() {
        mainViewController?.setMapRotation(0f)
    }

    override fun getPeliasLocationProvider(): PeliasLocationProvider {
        return mapzenLocation
    }

    override fun onReroute(location: Location) {
        if (waitingForRoute) {
            return
        }
        waitingForRoute = true
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
        Log.e(TAG, "Error fetching new route: " + statusCode)
        waitingForRoute = false
    }

    override fun success(route: Route) {
        mainViewController?.hideProgress()
        routeManager.route = route
        generateRoutingMode()
        mainViewController?.drawRoute(route)
        waitingForRoute = false
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
        vsm.viewState = ViewStateManager.ViewState.DEFAULT
        routingEnabled = false;
        routeManager.reverse = false
        onFindMeButtonClick()
    }

    override fun onMapMotionEvent(): Boolean {
        mainViewController?.rotateCompass()
        return false
    }

    override fun onReverseGeoRequested(screenX: Float?, screenY: Float?): Boolean {
        if (screenX != null && screenY != null) {
            if (reverseGeo || vsm.viewState == ViewStateManager.ViewState.DEFAULT) {
                mainViewController?.reverseGeolocate(screenX, screenY)
                reverseGeo = true
                return true
            }
        }
        return false
    }

    override fun onPlaceSearchRequested(gid: String): Boolean {
        if (reverseGeo || vsm.viewState == ViewStateManager.ViewState.DEFAULT) {
            mainViewController?.drawTappedPoiPin()
            mainViewController?.placeSearch(gid)
            reverseGeo = true
            return true
        }
        return false
    }

    override fun configureMapzenMap() {
        val currentLocation = mapzenLocation.getLastLocation()
        if (currentLocation is Location) {
            if (!initialized) {
                // Show location puck and center map
                mainViewController?.centerMapOnLocation(LngLat(currentLocation.longitude,
                        currentLocation.latitude), MainPresenter.DEFAULT_ZOOM)
                initialized = true
            }
        }
    }

    override fun onIntentQueryReceived(query: String?) {
        if (query != null && !query.isEmpty()) {
            val result = intentQueryParser.parse(query)
            if (result != null) {
                updateQueryMapPosition(result)
                updateQuerySearchTerm(result)
            }
        }
    }

    private fun updateQueryMapPosition(result: IntentQuery) {
        val focusPoint = result.focusPoint
        if (focusPoint.latitude != 0.0 && focusPoint.longitude != 0.0) {
            mainViewController?.centerMapOnLocation(focusPoint, MainPresenter.DEFAULT_ZOOM)
        }
    }

    private fun updateQuerySearchTerm(result: IntentQuery) {
        val queryString = result.queryString
        currentSearchTerm = queryString
        mainViewController?.hideSettingsBtn()
        mainViewController?.hideSearchResults()
        mainViewController?.executeSearch(queryString)
    }
}
