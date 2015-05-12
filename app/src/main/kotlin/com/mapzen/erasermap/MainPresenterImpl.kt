package com.mapzen.erasermap

import com.mapzen.pelias.gson.Result
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe

public class MainPresenterImpl() : MainPresenter {
    override var viewController: ViewController? = null
    override var currentSearchTerm: String? = null
    override var bus: Bus? = null
    set(bus) {
        bus?.register(this)
    }

    private var searchResults: Result? = null

    override fun onSearchResultsAvailable(searchResults: Result?) {
        this.searchResults = searchResults
        viewController?.showSearchResults(searchResults?.getFeatures())
        viewController?.hideProgress()
        val featureCount = searchResults?.getFeatures()?.size()
        if (featureCount != null && featureCount > 1) {
            viewController?.showActionViewAll()
        } else {
            viewController?.hideActionViewAll()
        }
    }

    override fun restoreViewState() {
        if (searchResults != null) {
            viewController?.showSearchResults(searchResults?.getFeatures())
        }
    }

    override fun onExpandSearchView() {
        viewController?.hideOverflowMenu()
    }

    override fun onCollapseSearchView() {
        searchResults = null;
        viewController?.hideSearchResults()
        viewController?.showOverflowMenu()
        viewController?.hideActionViewAll()
    }

    override fun onQuerySubmit() {
        viewController?.showProgress()
    }

    override fun onSearchResultSelected(position: Int) {
        if (searchResults != null) {
            viewController?.centerOnCurrentFeature(searchResults?.getFeatures())
        }
    }

    override fun onViewAllSearchResults() {
        viewController?.showAllSearchResults(searchResults?.getFeatures())
    }

    [Subscribe] public fun onRoutePreviewEvent(event: RoutePreviewEvent) {
        viewController?.collapseSearchView()
        viewController?.showRoutePreview(event.feature)
    }
}
