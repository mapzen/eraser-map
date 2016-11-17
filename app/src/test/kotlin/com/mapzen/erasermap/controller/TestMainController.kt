package com.mapzen.erasermap.controller

import com.mapzen.android.lost.api.Status
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.MapController
import com.mapzen.valhalla.Route

class TestMainController : MainViewController {

    var searchResults: List<Feature>? = null
    var reverseGeoCodeResults: List<Feature>? = null
    var lngLat: LngLat? = null
    var zoom: Float = 0f
    var tilt: Float = 0f
    var muted: Boolean = false
    var rotation: Float = 0f
    var routeLine: Route? = null
    var queryText: String = ""
    var reverseGeolocatePoint: LngLat? = null
    var placeSearchPoint: LngLat? = null

    var isProgressVisible: Boolean = false
    var isViewAllVisible: Boolean = false
    var isSearchVisible: Boolean = false
    var isRoutePreviewDestinationVisible: Boolean = false
    var isDirectionListVisible: Boolean = false
    var isRoutingModeVisible: Boolean = false
    var isSettingsVisible: Boolean = false
    var isFindMeTrackingEnabled: Boolean = false
    var popBackStack: Boolean = false
    var routeRequestCanceled: Boolean = false
    var isNew: Boolean = false
    var isCurrentLocationEnabled: Boolean = false
    var isAttributionAboveOptions = false
    var isFindMeAboveOptions = false
    var isRouting = false
    var isRouteBtnVisibleAndMapCentered = false
    var isOptionsMenuIconList = false
    var isShowingSearchResultsList = false
    var settingsApiTriggered: Boolean = false
    var isActionBarHidden = false
    var isRoutePreviewVisible: Boolean = false
    var isRoutePreviewDistanceTieVisible = false
    var routePreviewRoute: Route? = null
    var routePinLocations: Array<ValhallaLocation>? = null
    var isVoiceNavigationStopped = false
    var isRouteIconVisible = true
    var isRouteModeViewVisible = true
    var isRoutePreviewViewVisible = true
    var mapHasPanResponder = true
    var mapCameraType = MapController.CameraType.PERSPECTIVE
    var routePinsVisible = true
    var isAttributionAboveSearchResults = false
    var isFindMeAboveSearchResults = false
    var currentSearchItemPosition = 0
    var currentSearchIndex = 0
    var toastifyResId = 0
    var focusSearchView = false
    var debugSettingsEnabled = false
    var compassRotated = false
    var compassShowing = false
    var reverseGeoPointOnMap: LngLat? = null
    var searchResultsViewHidden = false
    var attributionAlignedBottom = false
    var placeSearchResults: List<Feature>? = null

    var screenPosLngLat: LngLat? = null

    override fun showSearchResults(features: List<Feature>?) {
        searchResults = features
    }

    override fun showSearchResults(features: List<Feature>?, currentIndex: Int) {
        currentSearchIndex = currentIndex
        searchResults = features
    }

    override fun addSearchResultsToMap(features: List<Feature>?, activeIndex: Int) {
        searchResults = features
    }

    override fun hideSearchResults() {
        searchResults = null
    }

    override fun hideReverseGeolocateResult() {
        reverseGeoCodeResults = null
    }

    override fun showProgress() {
        isProgressVisible = true
    }

    override fun hideProgress() {
        isProgressVisible = false
    }

    override fun showActionViewAll() {
        isViewAllVisible = true
    }

    override fun hideActionViewAll() {
        isViewAllVisible = false
    }

    override fun toggleShowAllSearchResultsList(features: List<Feature>?) {
    }

    override fun collapseSearchView() {
        isSearchVisible = false
    }

    override fun expandSearchView() {
    }

    override fun clearQuery() {
        queryText = ""
    }

    override fun showRoutePreviewDestination(destination: SimpleFeature) {
        isRoutePreviewDestinationVisible = true
    }

    override fun route() {
        isRouting = true
    }

    override fun shutDown() {
    }

    override fun showDirectionsList() {
        isDirectionListVisible = true
    }

    override fun hideDirectionsList() {
        isDirectionListVisible = false
    }

    override fun hideRoutingMode() {
        isRoutingModeVisible = false
    }

    override fun startRoutingMode(feature: Feature, isNew: Boolean) {
        isRoutingModeVisible = true
        this.isNew = isNew
    }

    override fun resumeRoutingMode(feature: Feature) {
        isRoutingModeVisible = true
    }

    override fun resumeRoutingModeForMap() {
        isRouteBtnVisibleAndMapCentered = true
    }

    override fun centerMapOnLocation(lngLat: LngLat, zoom: Float) {
        this.lngLat = lngLat
        this.zoom = zoom
    }

    override fun setMapTilt(radians: Float) {
        tilt = radians
    }

    override fun resetMute() {
        muted = false
    }

    override fun toggleMute() {
        muted = !muted
    }

    override fun setMapRotation(radians: Float) {
        rotation = radians
    }

//    override fun showReverseGeocodeFeature(features: List<Feature>?) {
//        isReverseGeocodeVisible = true
//        reverseGeoCodeResults = features;
//    }

    override fun showPlaceSearchFeature(features: List<Feature>) {
        placeSearchResults = features
//        showReverseGeocodeFeature(features)
    }

    override fun drawRoute(route: Route) {
        routeLine = route
    }

    override fun clearRoute() {
        routeLine = null
    }

    override fun rotateCompass() {
        compassRotated = true
    }

    override fun showCompass() {
        compassShowing = true
    }

    override fun reverseGeolocate(screenX: Float, screenY: Float) {
        reverseGeolocatePoint = LngLat(screenX.toDouble(), screenY.toDouble())
    }

    override fun placeSearch(gid: String) {
        placeSearchPoint = LngLat(0.0, 0.0)
    }
 
//    override fun emptyPlaceSearch() {
//        isReverseGeocodeVisible = true
//    }

//    override fun overridePlaceFeature(feature: Feature) {
//        isPlaceResultOverridden = true
//    }

//    override fun drawTappedPoiPin() {
//        //empty
//    }

    override fun hideSettingsBtn() {
        isSettingsVisible = false
    }

    override fun showSettingsBtn() {
        isSettingsVisible = true
    }

    override fun onBackPressed() {
        popBackStack = true
    }

    override fun stopSpeaker() {

    }

    override fun checkPermissionAndEnableLocation() {

    }

    override fun executeSearch(query: String) {
        queryText = query
    }

    override fun deactivateFindMeTracking() {
        isFindMeTrackingEnabled = false
    }

    override fun cancelRouteRequest() {
        routeRequestCanceled = true
    }

    override fun layoutAttributionAboveOptions() {
        isAttributionAboveOptions = true
    }

    override fun layoutFindMeAboveOptions() {
        isFindMeAboveOptions = true
    }

    override fun restoreRoutePreviewButtons() {
    }

    override fun onCloseAllSearchResultsList() {
    }

    override fun handleLocationResolutionRequired(status: Status) {
        settingsApiTriggered = true
    }

    override fun setMyLocationEnabled(enabled: Boolean) {
        isCurrentLocationEnabled = enabled
    }

    override fun setOptionsMenuIconToList() {
        isOptionsMenuIconList = true
    }

    override fun onShowAllSearchResultsList(features: List<Feature>) {
        isShowingSearchResultsList = true
    }

    override fun hideActionBar() {
        isActionBarHidden = true
    }

    override fun showRoutePreviewView() {
        isRoutePreviewVisible = true
    }

    override fun showRoutePreviewDistanceTimeLayout() {
        isRoutePreviewDistanceTieVisible = true
    }

    override fun setRoutePreviewViewRoute(route: Route) {
        routePreviewRoute = route
    }

    override fun showRoutePinsOnMap(locations: Array<ValhallaLocation>) {
        routePinLocations = locations
    }

    override fun updateRoutePreviewStartNavigation() {

    }

    override fun stopVoiceNavigationController() {
        isVoiceNavigationStopped = true
    }

    override fun hideRouteIcon() {
        isRouteIconVisible = false
    }

    override fun hideRouteModeView() {
        isRouteModeViewVisible = false
    }

    override fun showActionBar() {
        isActionBarHidden = false
    }

    override fun hideRoutePreviewView() {
        isRoutePreviewViewVisible = false
    }

    override fun resetMapPanResponder() {
        mapHasPanResponder = false
    }

    override fun setDefaultCamera() {
        mapCameraType = MapController.CameraType.ISOMETRIC
    }

    override fun layoutFindMeAlignBottom() {
        isFindMeAboveOptions = false
    }

    override fun hideMapRoutePins() {
        routePinsVisible = false
    }

    override fun layoutAttributionAboveSearchResults(features: List<Feature>) {
        isAttributionAboveSearchResults = true
    }

    override fun layoutFindMeAboveSearchResults(features: List<Feature>) {
        isFindMeAboveSearchResults = true
    }

    override fun setCurrentSearchItem(position: Int) {
        currentSearchItemPosition = position
    }

    override fun setMapPosition(lngLat: LngLat, duration: Int) {
        this.lngLat = lngLat
    }

    override fun setMapZoom(zoom: Float) {
        this.zoom = zoom
    }

    override fun getCurrentSearchPosition(): Int {
        return currentSearchItemPosition
    }

    override fun toastify(resId: Int) {
        toastifyResId = resId
    }

    override fun focusSearchView() {
        focusSearchView = true
    }

    override fun toggleShowDebugSettings() {
        debugSettingsEnabled = !debugSettingsEnabled
    }

    override fun getMapPosition(): LngLat? {
        return lngLat
    }

    override fun getMapZoom(): Float? {
        return zoom
    }

    override fun showReverseGeoResult(lngLat: LngLat?) {
        reverseGeoPointOnMap = lngLat
    }

    override fun screenPositionToLngLat(x: Float, y: Float): LngLat? {
        if (screenPosLngLat != null) {
            return screenPosLngLat
        }
        return LngLat(0.0, 0.0)
    }

    override fun hideSearchResultsView() {
        searchResultsViewHidden = true
    }

    override fun layoutAttributionAlignBottom() {
        attributionAlignedBottom = true
    }

    override fun setMapZoom(zoom: Float, duration: Int) {
        this.zoom = zoom
    }
}
