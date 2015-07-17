package com.mapzen.erasermap.view

import android.location.Location
import com.mapzen.pelias.gson.Feature

public interface MainViewController {
    public fun showSearchResults(features: List<Feature>)
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
    public fun showRoutePreview(feature: Feature)
    public fun hideRoutePreview()
    public fun hideRoutingMode()
    public fun showRoutingMode(feature: Feature)
    public fun shutDown()
    public fun centerMapOnCurrentLocation()
    public fun centerMapOnCurrentLocation(zoom: Float)
    public fun centerMapOnLocation(location: Location, zoom: Float)
}
