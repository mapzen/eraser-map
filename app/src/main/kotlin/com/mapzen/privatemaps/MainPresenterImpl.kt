package com.mapzen.privatemaps

import com.mapzen.pelias.gson.Result

public class MainPresenterImpl : MainPresenter {
    override var viewController: ViewController? = null
    override var currentSearchTerm: String? = null

    private var searchResults: Result? = null

    override fun onSearchResultsAvailable(searchResults: Result?) {
        this.searchResults = searchResults
        viewController?.showSearchResults(searchResults?.getFeatures())
        viewController?.hideProgress()
        viewController?.showActionViewAll()
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
}
