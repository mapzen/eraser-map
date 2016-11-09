package com.mapzen.erasermap.presenter

import android.location.Location
import android.util.Log
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.Status
import com.mapzen.erasermap.controller.MainViewController
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.model.ConfidenceHandler
import com.mapzen.erasermap.model.IntentQuery
import com.mapzen.erasermap.model.IntentQueryParser
import com.mapzen.erasermap.model.LocationClientManager
import com.mapzen.erasermap.model.LocationConverter
import com.mapzen.erasermap.model.LocationSettingsChecker
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.model.PermissionManager
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
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Geometry
import com.mapzen.pelias.gson.Properties
import com.mapzen.pelias.gson.Result
import com.mapzen.tangram.LngLat
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.ArrayList

open class MainPresenterImpl(val mapzenLocation: MapzenLocation, val bus: Bus,
    val routeManager: RouteManager, val settings: AppSettings, val vsm: ViewStateManager,
    val intentQueryParser: IntentQueryParser, val converter: LocationConverter,
    val locationClientManager: LocationClientManager,
    val locationSettingsChecker: LocationSettingsChecker, val permissionManager: PermissionManager,
    val confidenceHandler: ConfidenceHandler) : MainPresenter, RouteCallback {

  companion object {
    private val TAG = MainPresenterImpl::class.java.simpleName

    @JvmStatic val MAP_DATA_PROP_SEARCHINDEX = "searchIndex"
  }

  override var currentFeature: Feature? = null
  override var routingEnabled: Boolean = false
  override var mainViewController: MainViewController? = null
  override var routeViewController: RouteViewController? = null
  override var currentSearchTerm: String? = null
  override var resultListVisible = false
  override var reverseGeo = false
  override var reverseGeoLngLat: LngLat?
    get() = confidenceHandler.reverseGeoLngLat
    set(value) {
      confidenceHandler.reverseGeoLngLat = value
    }
  override var currentSearchIndex: Int = 0

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
    mainViewController?.deactivateFindMeTracking()
    updateViewAllAction(result)
  }

  private fun updateViewAllAction(result: Result?) {
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
    if (searchResults?.features?.isEmpty() as Boolean) {
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
    if (searchResults?.features?.isEmpty() as Boolean) {
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
      DEFAULT, SEARCH, SEARCH_RESULTS -> {}
      ROUTE_PREVIEW -> onRestoreViewStateRoutePreview()
      ROUTE_PREVIEW_LIST -> onRestoreViewStateRoutePreviewList()
      ROUTING -> onRestoreViewStateRouting()
      ROUTE_DIRECTION_LIST -> onRestoreViewStateRouteDirectionList()
    }
  }

  private fun onRestoreViewStateRoutePreview() {
    mainViewController?.showRoutePreviewView()
    mainViewController?.showRoutePreviewDistanceTimeLayout()
    generateRoutePreview(false)
    mainViewController?.restoreRoutePreviewButtons()
  }

  private fun onRestoreViewStateRoutePreviewList() {
    generateRoutePreview(false)
    onClickViewList()
  }

  private fun onRestoreViewStateRouting() {
    resumeRoutingMode()
  }

  private fun onRestoreViewStateRouteDirectionList() {
    resumeRoutingMode()
    routeViewController?.showRouteDirectionList()
  }

  override fun onRestoreOptionsMenu() {
    when (vsm.viewState) {
      DEFAULT, SEARCH -> {}
      SEARCH_RESULTS -> onRestoreOptionsMenuStateSearchResults()
      ROUTE_PREVIEW -> {
        mainViewController?.hideActionBar()
        mainViewController?.hideSettingsBtn()
      }
      ROUTE_PREVIEW_LIST, ROUTING, ROUTE_DIRECTION_LIST -> {
        mainViewController?.hideSettingsBtn()
      }
    }
  }

  private fun onRestoreOptionsMenuStateSearchResults() {
    mainViewController?.hideSettingsBtn()
    mainViewController?.setOptionsMenuIconToList()
    updateViewAllAction(searchResults)
    if (resultListVisible && searchResults?.features != null) {
      val features = searchResults?.features as List<Feature>
      mainViewController?.onShowAllSearchResultsList(features)
    }
  }

  override fun onRestoreMapState() {
    when (vsm.viewState) {
      DEFAULT, SEARCH -> {}
      SEARCH_RESULTS -> onRestoreMapStateSearchResults()
      ROUTE_PREVIEW, ROUTE_PREVIEW_LIST -> adjustLayoutAndRoute()
      ROUTING, ROUTE_DIRECTION_LIST -> mainViewController?.resumeRoutingModeForMap()
    }
  }

  private fun onRestoreMapStateSearchResults() {
    if (searchResults?.features != null) {
      if (!reverseGeo) {
        mainViewController?.showSearchResults(searchResults?.features, currentSearchIndex)
      } else {
        mainViewController?.showReverseGeocodeFeature(searchResults?.features)
        centerOnCurrentFeature(searchResults?.features)
      }
    }
  }

  private fun adjustLayoutAndRoute() {
    mainViewController?.layoutAttributionAboveOptions()
    mainViewController?.layoutFindMeAboveOptions()
    mainViewController?.route()
  }

  override fun onExpandSearchView() {
    if (vsm.viewState != ViewStateManager.ViewState.SEARCH_RESULTS) {
      vsm.viewState = ViewStateManager.ViewState.SEARCH
    }
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
    currentSearchIndex = position
    if (searchResults != null) {
      mainViewController?.addSearchResultsToMap(searchResults?.features, position)
      centerOnCurrentFeature(searchResults?.features)
    }
  }

  override fun onSearchResultTapped(position: Int) {
    if (searchResults != null) {
      mainViewController?.addSearchResultsToMap(searchResults?.features, position)
      centerOnFeature(searchResults?.features, position)
    }
  }

  override fun onViewAllSearchResultsList() {
    mainViewController?.toggleShowAllSearchResultsList(searchResults?.features)
  }

  private fun connectAndPostRunnable(run: () -> Unit) {
    locationClientManager.connect()
    locationClientManager.addRunnableToRunOnConnect(Runnable { run() })
  }

  @Subscribe fun onRoutePreviewEvent(event: RoutePreviewEvent) {
    if (!locationClientManager.getClient().isConnected) {
      connectAndPostRunnable { onRoutePreviewEvent(event) }
      return
    }

    val locationStatusCode = locationSettingsChecker.getLocationStatusCode(mapzenLocation,
        locationClientManager)
    if (locationStatusCode == Status.RESOLUTION_REQUIRED) {
        val locationStatus = locationSettingsChecker.getLocationStatus(mapzenLocation,
            locationClientManager)
        mainViewController?.handleLocationResolutionRequired(locationStatus)
        return
    }

    vsm.viewState = ViewStateManager.ViewState.ROUTE_PREVIEW
    if (reverseGeo) {
      restoreReverseGeoOnBack = true
    }
    reverseGeo = false
    destination = event.destination
    mainViewController?.collapseSearchView()
    mainViewController?.hideSearchResults()
    mainViewController?.hideReverseGeolocateResult()
    mainViewController?.deactivateFindMeTracking()
    generateRoutePreview()
  }

  @Subscribe fun onRouteCancelEvent(event: RouteCancelEvent) {
    mainViewController?.onBackPressed()
  }

  override fun updateLocation() {
    if (!locationClientManager.getClient().isConnected) {
      connectAndPostRunnable { updateLocation() }
      return
    }

    val location = mapzenLocation.getLastLocation()
    if (location != null) {
      routeViewController?.onLocationChanged(location)
    }
  }

  override fun onBackPressed() {
    if (vsm.viewState == SEARCH || vsm.viewState == SEARCH_RESULTS) {
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

    mainViewController?.hideProgress()
    mainViewController?.cancelRouteRequest()
    mainViewController?.showActionBar()
    routeManager.reverse = false
    mainViewController?.hideRoutePreviewView()
    mainViewController?.hideMapRoutePins()
    val features = arrayListOf(currentFeature) as List<Feature>
    mainViewController?.layoutAttributionAboveSearchResults(features)
    mainViewController?.layoutFindMeAboveSearchResults(features)
    mainViewController?.clearRoute()
    if (searchResults != null) {
      if (reverseGeo) {
        mainViewController?.showReverseGeocodeFeature(searchResults?.features)
      } else {
        mainViewController?.showSearchResults(searchResults?.features, currentSearchIndex)
        var numFeatures = 0
        numFeatures = (searchResults?.features?.size as Int)
        if (numFeatures > 1) {
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
    mapzenLocation.stopLocationUpdates() //must call before calling mainViewController?.hideRoutingMode()
    mainViewController?.hideRoutingMode()
    mainViewController?.stopSpeaker()
    val location = routeManager.origin
    val feature = routeManager.destination
    if (location is ValhallaLocation && feature is Feature) {
      showRoutePreview(location, feature)
    }
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
    if (!locationClientManager.getClient().isConnected) {
      connectAndPostRunnable { onClickStartNavigation() }
      return
    }

    bus.post(RouteEvent())
    mainViewController?.resetMute() //must call before generateRoutingMode()
    generateRoutingMode(true)
    vsm.viewState = ViewStateManager.ViewState.ROUTING
    routeViewController?.hideResumeButton()
    mapzenLocation.startLocationUpdates()
  }

  @Subscribe fun onLocationChangeEvent(event: LocationChangeEvent) {
    if (routingEnabled) {
      routeViewController?.onLocationChanged(event.location)
    }
  }

  override fun onResume() {
    if (!isRouting() && !isRoutingDirectionList()) {
      mainViewController?.checkPermissionAndEnableLocation()
    }
  }

  private fun isRouting(): Boolean {
    return vsm.viewState == ViewStateManager.ViewState.ROUTING
  }

  private fun isRoutingDirectionList(): Boolean {
    return vsm.viewState == ViewStateManager.ViewState.ROUTE_DIRECTION_LIST
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

  override fun onReroute(location: ValhallaLocation) {
    if (waitingForRoute) {
      return
    }
    waitingForRoute = true
    mainViewController?.showProgress()
    fetchNewRoute(location)
  }

  private fun fetchNewRoute(location: ValhallaLocation) {
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
    handleRouteRetrieved(route)
    generateRoutingMode(false)
    waitingForRoute = false
  }

  private fun generateRoutePreview() {
    generateRoutePreview(true)
  }

  private fun generateRoutePreview(updateMap: Boolean) {
    if (!locationClientManager.getClient().isConnected) {
      connectAndPostRunnable { generateRoutePreview(updateMap) }
      return
    }

    val location = mapzenLocation.getLastLocation()
    val feature = destination
    if (location is Location && feature is Feature) {
      val mapzenLocation = converter.mapzenLocation(location)
      showRoutePreview(mapzenLocation, feature, updateMap)
    }
  }

  private fun generateRoutingMode(isNew: Boolean) {
    routingEnabled = true
    val feature = destination
    if (feature is Feature) {
      mainViewController?.startRoutingMode(feature, isNew)
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
    routingEnabled = false
    routeManager.reverse = false
    checkPermissionAndEnableLocation()
    mainViewController?.stopVoiceNavigationController()
    mainViewController?.clearRoute()
    mainViewController?.hideRouteIcon()
    mainViewController?.hideRouteModeView()
    mainViewController?.showActionBar()
    mainViewController?.hideRoutePreviewView()
    mainViewController?.resetMapPanResponder()
    mainViewController?.setDefaultCamera()
    mainViewController?.layoutFindMeAlignBottom()
    mainViewController?.setMapTilt(0f)
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
    if (!locationClientManager.getClient().isConnected) {
      connectAndPostRunnable { configureMapzenMap() }
      return
    }

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

  override fun onRouteRequest(callback: RouteCallback) {
    mainViewController?.showProgress()
    mainViewController?.cancelRouteRequest()
    routeManager.fetchRoute(callback)
  }

  override fun onRouteSuccess(route: Route) {
    handleRouteRetrieved(route)
    mainViewController?.setRoutePreviewViewRoute(route)
    mainViewController?.hideActionBar()
    mainViewController?.showRoutePreviewView()
    mainViewController?.showRoutePreviewDistanceTimeLayout()
    mainViewController?.showRoutePinsOnMap(route.getGeometry().toTypedArray())
    mainViewController?.updateRoutePreviewStartNavigation()
  }

  private fun showRoutePreview(location: ValhallaLocation, feature: Feature) {
    showRoutePreview(location, feature, true)
  }

  private fun showRoutePreview(location: ValhallaLocation, feature: Feature,
       updateMap: Boolean) {
    if (updateMap) {
      mainViewController?.layoutAttributionAboveOptions()
      mainViewController?.layoutFindMeAboveOptions()
    }
    routeManager.origin = location

    if (location.hasBearing()) {
      routeManager.bearing = location.bearing
    } else {
      routeManager.bearing = null
    }

    if (!confidenceHandler.useRawLatLng(feature.properties.confidence)) {
      mainViewController?.showRoutePreviewDestination(SimpleFeature.fromFeature(feature))
      routeManager.destination = feature
    } else {
      val rawFeature = generateRawFeature()
      mainViewController?.showRoutePreviewDestination(SimpleFeature.fromFeature(rawFeature))
      routeManager.destination = rawFeature
    }

    if (updateMap) {
      mainViewController?.route()
    }
  }

  override fun generateRawFeature(): Feature {
    val rawFeature: Feature = Feature()
    rawFeature.geometry = Geometry()
    val coords = ArrayList<Double>()
    coords.add(reverseGeoLngLat?.longitude as Double)
    coords.add(reverseGeoLngLat?.latitude as Double)
    rawFeature.geometry.coordinates = coords
    val properties = Properties()
    val formatter = DecimalFormat(".####")
    formatter.roundingMode = RoundingMode.HALF_UP
    val lng = formatter.format(reverseGeoLngLat?.longitude as Double)
    val lat = formatter.format(reverseGeoLngLat?.latitude  as Double)
    properties.name = "$lng, $lat"
    rawFeature.properties = properties
    return rawFeature
  }

  override fun onFeaturePicked(properties: Map<String, String>, poiPoint: FloatArray) {
    if (properties.contains(MAP_DATA_PROP_SEARCHINDEX)) {
      val searchIndex = properties[MAP_DATA_PROP_SEARCHINDEX]!!.toInt()
      onSearchResultTapped(searchIndex)
    } else {
      onReverseGeoRequested(poiPoint.get(0).toFloat(), poiPoint.get(1).toFloat())
    }
  }

  override fun checkPermissionAndEnableLocation() {
    if (permissionManager.granted && !routingEnabled) {
      mainViewController?.setMyLocationEnabled(true)
      if (!locationClientManager.getClient().isConnected) {
        locationClientManager.connect()
        locationClientManager.addRunnableToRunOnConnect(Runnable { initMockMode() })
      } else {
        initMockMode()
      }
    }
  }

  override fun onClickFindMe() {
    mainViewController?.setMapTilt(0f)
  }

  private fun initMockMode() {
    if (settings.isMockLocationEnabled) {
      val client = locationClientManager.getClient()
      LocationServices.FusedLocationApi?.setMockMode(client, true)
      LocationServices.FusedLocationApi?.setMockLocation(client, settings.mockLocation)
    }
  }

  private fun handleRouteRetrieved(route: Route) {
    routeManager.route = route
    mainViewController?.drawRoute(route)
    mainViewController?.hideProgress()
  }

  private fun featuresExist(features: List<Feature>?): Boolean {
    return features != null && features!!.size > 0
  }

  private fun centerOnCurrentFeature(features: List<Feature>?) {
    if (!featuresExist(features)) {
      return
    }
    val position = mainViewController?.getCurrentSearchPosition()
    centerOnFeature(features, position as Int)
  }

  private fun centerOnFeature(features: List<Feature>?, position: Int) {
    if (!featuresExist(features)) {
      return
    }

    mainViewController?.setCurrentSearchItem(position)
    val feature = SimpleFeature.fromFeature(features!![position])
    mainViewController?.setMapPosition(LngLat(feature.lng(), feature.lat()), 1000)
    mainViewController?.setMapZoom(MainPresenter.DEFAULT_ZOOM)
  }
}
