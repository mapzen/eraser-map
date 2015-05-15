package com.mapzen.erasermap.view

import com.mapzen.pelias.gson.Feature

public trait ViewController {
    public fun showSearchResults(features: List<Feature>)
    public fun centerOnCurrentFeature(features: List<Feature>)
    public fun showAllSearchResults(features: List<Feature>)
    public fun hideSearchResults()
    public fun showProgress()
    public fun hideProgress()
    public fun showOverflowMenu()
    public fun hideOverflowMenu()
    public fun showActionViewAll()
    public fun hideActionViewAll()
    public fun collapseSearchView()
    public fun showRoutePreview(feature: Feature)
    public fun hideRoutePreview()
    public fun shutDown()
}
