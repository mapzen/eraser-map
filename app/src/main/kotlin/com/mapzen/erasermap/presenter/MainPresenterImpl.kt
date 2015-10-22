package com.mapzen.erasermap.presenter

import android.location.Location
import android.util.Log
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.model.LocationChangeEvent
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.model.RouteEvent
import com.mapzen.erasermap.model.RoutePreviewEvent
import com.mapzen.erasermap.model.RouterFactory
import com.mapzen.erasermap.view.MainViewController
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import java.util.ArrayList

public open class MainPresenterImpl(val mapzenLocation: MapzenLocation,
        val routerFactory: RouterFactory, val settings: AppSettings)
        : MainPresenter, RouteCallback {

    override var currentFeature: Feature? = null
    override var route: Route? = null
    override var routingEnabled : Boolean = false
    override var mainViewController: MainViewController? = null
    override var routeViewController: RouteViewController? = null
    override var currentSearchTerm: String? = null
    override var bus: Bus? = null
        set(value) {
            $bus = value
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
        viewState = ViewState.SEARCH_RESULTS
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
            val current = currentFeature
            if (current is Feature) {
                features.add(current)
            }
            mainViewController?.showReverseGeocodeFeature(features)
            searchResults?.setFeatures(features)
        } else {
            val current = searchResults?.getFeatures()?.get(0)
            if (current is Feature) {
                features.add(current)
            }
            searchResults?.setFeatures(features)
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

        if (viewState == ViewState.ROUTE_DIRECTION_LIST) {
            routeViewController?.showDirectionList()
        }
    }

    override fun onExpandSearchView() {
        viewState = ViewState.SEARCH
        mainViewController?.hideOverflowMenu()
    }

    override fun onCollapseSearchView() {
        if (viewState != ViewState.ROUTE_PREVIEW) {
            viewState = ViewState.DEFAULT
        }

        searchResults = null
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
        destination = event.destination
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
        viewState = ViewState.DEFAULT
        mainViewController?.collapseSearchView()
        mainViewController?.hideSearchResults()
    }

    private fun onBackPressedStateRoutePreview() {
        viewState = ViewState.SEARCH_RESULTS
        mainViewController?.hideRoutePreview()
        mainViewController?.clearRouteLine()
    }

    private fun onBackPressedStateRouting() {
        viewState = ViewState.ROUTE_PREVIEW
        mainViewController?.hideRoutingMode()
    }

    private fun onBackPressedStateRouteDirectionList() {
        viewState = ViewState.ROUTING
        routeViewController?.collapseSlideLayout()
    }

    override fun onClickViewList() {
        mainViewController?.showDirectionList()
    }

    override fun onClickStartNavigation() {
        bus?.post(RouteEvent())
        generateRoutingMode()
        viewState = ViewState.ROUTING
    }

    @Subscribe public fun onLocationChangeEvent(event: LocationChangeEvent) {
        if (routingEnabled) {
            routeViewController?.onLocationChanged(event.location)
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

    override fun onCreate() {
        if (!initialized) {
            val currentLocation = mapzenLocation.getLastLocation()
            if (currentLocation is Location) {
                mainViewController?.centerMapOnLocation(currentLocation, MainPresenter.DEFAULT_ZOOM)
            }
            initialized = true
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
        return viewState == ViewState.ROUTING
    }

    private fun isRoutingDirectionList(): Boolean {
        return viewState == ViewState.ROUTE_DIRECTION_LIST
    }

    override fun onFindMeButtonClick() {
        val currentLocation = mapzenLocation.getLastLocation()
        if (currentLocation is Location) {
            mainViewController?.centerMapOnLocation(currentLocation, MainPresenter.DEFAULT_ZOOM)
            mainViewController?.setMapTilt(0f)
            mainViewController?.setMapRotation(0f)
        }
    }

    override fun getPeliasLocationProvider(): PeliasLocationProvider {
        return mapzenLocation
    }

    override fun onReroute(location: Location) {
        mainViewController?.showProgress()
        fetchNewRoute(location)
    }

    private fun fetchNewRoute(location: Location) {
        val simpleFeature = SimpleFeature.fromFeature(destination)
        val start: DoubleArray = doubleArrayOf(location.latitude, location.longitude)
        val dest: DoubleArray = doubleArrayOf(simpleFeature.lat, simpleFeature.lon)
        val name = destination?.properties?.name
        val street = simpleFeature.title
        val city = simpleFeature.city
        val state = simpleFeature.admin
        routerFactory.getInitializedRouter(Router.Type.DRIVING)
                .setLocation(start)
                .setLocation(dest, name, street, city, state)
                .setDistanceUnits(settings.distanceUnits)
                .setCallback(this)
                .fetch()
    }

    override fun failure(statusCode: Int) {
        mainViewController?.hideProgress()
        Log.e("MainPresenterImpl", "Error fetching new route: " + statusCode)
    }

    override fun success(route: Route) {
        mainViewController?.hideProgress()
        this.route = route
        generateRoutingMode()
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
}
