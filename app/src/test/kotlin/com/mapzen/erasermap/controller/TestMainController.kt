package com.mapzen.erasermap.controller

import android.graphics.PointF
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.tangram.LngLat
import com.mapzen.valhalla.Route

class TestMainController : MainViewController {
    override fun onCloseAllSearchResults() {
        throw UnsupportedOperationException()
    }

    var searchResults: List<Feature>? = null
    var reverseGeoCodeResults: List<Feature>? = null
    var lngLat: LngLat? = null
    var zoom: Float = 0f
    var tilt: Float = 0f
    var muted: Boolean = false
    var rotation: Float = 0f
    var routeLine: Route? = null
    var queryText: String = ""
    var reverseGeolocatePoint: PointF? = null
    var placeSearchPoint: PointF? = null

    var isProgressVisible: Boolean = false
    var isViewAllVisible: Boolean = false
    var isSearchVisible: Boolean = false
    var isRoutePreviewVisible: Boolean = false
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

    override fun showAllSearchResults(features: List<Feature>?) {
    }

    override fun collapseSearchView() {
        isSearchVisible = false
    }

    override fun expandSearchView() {
    }

    override fun clearQuery() {
        queryText = ""
    }

    override fun showRoutePreview(destination: SimpleFeature) {
        isRoutePreviewVisible = true
    }

    override fun route() {
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

    override fun startRoutingMode(feature: Feature) {
        isRoutingModeVisible = true
    }

    override fun resumeRoutingMode(feature: Feature) {
        isRoutingModeVisible = true
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
        reverseGeolocatePoint = PointF(screenX, screenY)
    }

    override fun placeSearch(gid: String) {
        placeSearchPoint = PointF(0.0f, 0.0f)
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
    }

    override fun layoutFindMeAboveOptions() {
    }
}
