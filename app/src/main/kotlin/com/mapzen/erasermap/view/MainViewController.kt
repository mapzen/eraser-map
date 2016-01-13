package com.mapzen.erasermap.view

import android.location.Location
import android.view.MotionEvent
import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Route

public interface MainViewController {
    public fun showSearchResults(features: List<Feature>)
    public fun addSearchResultsToMap(features: List<Feature>, activeIndex: Int)
    public fun showDirectionList()
    public fun centerOnCurrentFeature(features: List<Feature>)
    public fun showAllSearchResults(features: List<Feature>)
    public fun hideSearchResults()
    public fun showReverseGeocodeFeature(features: List<Feature>)
    public fun showProgress()
    public fun hideProgress()
    public fun showOverflowMenu()
    public fun hideOverflowMenu()
    public fun showActionViewAll()
    public fun hideActionViewAll()
    public fun collapseSearchView()
    public fun clearQuery()
    public fun showRoutePreview(location: Location, feature: Feature)
    public fun hideRoutePreview()
    public fun hideRoutingMode()
    public fun startRoutingMode(feature: Feature)
    public fun resumeRoutingMode(feature: Feature)
    public fun shutDown()
    public fun centerMapOnLocation(location: Location, zoom: Float)
    public fun showCurrentLocation(location: Location)
    public fun setMapTilt(radians: Float)
    public fun setMapRotation(radians: Float)
    public fun drawRoute(route: Route)
    public fun clearRoute()
    public fun rotateCompass()
    public fun reverseGeolocate(screenX: Float, screenY: Float)
}
