package com.mapzen.erasermap.controller

import com.mapzen.android.lost.api.Status
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.tangram.LngLat
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
    var isCenteredOnCurrentFeature: Boolean = false
    var isCenteredOnTappedFeature: Boolean = false
    var isReverseGeocodeVisible: Boolean = false
    var isPlaceResultOverridden: Boolean = false
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

    override fun showSearchResults(features: List<Feature>?) {
        searchResults = features
    }

    override fun addSearchResultsToMap(features: List<Feature>?, activeIndex: Int) {
        searchResults = features;
    }

    override fun centerOnCurrentFeature(features: List<Feature>?) {
        isCenteredOnCurrentFeature = true
    }

    override fun centerOnFeature(features: List<Feature>?, position: Int) {
        isCenteredOnTappedFeature = true
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

    override fun hideRoutePreview() {
        isRoutePreviewVisible = false
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

    override fun showReverseGeocodeFeature(features: List<Feature>?) {
        isReverseGeocodeVisible = true
        reverseGeoCodeResults = features;
    }

    override fun showPlaceSearchFeature(features: List<Feature>) {
        showReverseGeocodeFeature(features)
    }

    override fun drawRoute(route: Route) {
        routeLine = route
    }

    override fun clearRoute() {
        routeLine = null
    }

    override fun rotateCompass() {
    }

    override fun reverseGeolocate(screenX: Float, screenY: Float) {
        reverseGeolocatePoint = LngLat(screenX.toDouble(), screenY.toDouble())
    }

    override fun placeSearch(gid: String) {
        placeSearchPoint = LngLat(0.0, 0.0)
    }
 
    override fun emptyPlaceSearch() {
        isReverseGeocodeVisible = true
    }

    override fun overridePlaceFeature(feature: Feature) {
        isPlaceResultOverridden = true
    }

    override fun drawTappedPoiPin() {
        //empty
    }

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
}
