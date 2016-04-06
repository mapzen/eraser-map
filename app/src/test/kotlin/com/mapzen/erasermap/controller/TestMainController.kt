package com.mapzen.erasermap.controller

import android.graphics.PointF
import android.location.Location
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Route

public class TestMainController : MainViewController {
    public var searchResults: List<Feature>? = null
    public var reverseGeoCodeResults: List<Feature>? = null
    public var location: Location? = null
    public var zoom: Float = 0f
    public var tilt: Float = 0f
    public var muted: Boolean = false
    public var rotation: Float = 0f
    public var routeLine: Route? = null
    public var queryText: String = ""
    public var puckPosition: Location? = null
    public var reverseGeolocatePoint: PointF? = null
    public var placeSearchPoint: PointF? = null

    public var isProgressVisible: Boolean = false
    public var isViewAllVisible: Boolean = false
    public var isSearchVisible: Boolean = false
    public var isRoutePreviewVisible: Boolean = false
    public var isDirectionListVisible: Boolean = false
    public var isRoutingModeVisible: Boolean = false
    public var isCenteredOnCurrentFeature: Boolean = false
    public var isCenteredOnTappedFeature: Boolean = false
    public var isReverseGeocodeVisible: Boolean = false
    public var isPlaceResultOverridden: Boolean = false
    public var isSettingsVisible: Boolean = false
    public var popBackStack: Boolean = false

    override fun showSearchResults(features: List<Feature>) {
        searchResults = features
    }

    override fun addSearchResultsToMap(features: List<Feature>, activeIndex: Int) {
        searchResults = features;
    }

    override fun centerOnCurrentFeature(features: List<Feature>) {
        isCenteredOnCurrentFeature = true
    }

    override fun centerOnFeature(features: List<Feature>, position: Int) {
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

    override fun showAllSearchResults(features: List<Feature>) {
    }

    override fun collapseSearchView() {
        isSearchVisible = false
    }

    override fun expandSearchView() {
    }

    override fun clearQuery() {
        queryText = ""
    }

    override fun showRoutePreview(location: Location, feature: Feature) {
        isRoutePreviewVisible = true
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

    override fun centerMapOnLocation(location: Location, zoom: Float) {
        this.location = location
        this.zoom = zoom
    }

    override fun showCurrentLocation(location: Location) {
        puckPosition = location
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

    override fun showReverseGeocodeFeature(features: List<Feature>) {
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
}
