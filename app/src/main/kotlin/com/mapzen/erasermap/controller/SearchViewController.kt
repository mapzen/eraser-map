package com.mapzen.erasermap.controller

import android.view.View
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.erasermap.view.SearchResultsAdapter
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.widget.AutoCompleteListView
import com.mapzen.pelias.widget.PeliasSearchView

interface SearchViewController {
    var mainController: MainViewController?
    var searchView: PeliasSearchView?
    var autoCompleteListView: AutoCompleteListView?
    var onSearchResultsSelectedListener: OnSearchResultSelectedListener?

    fun initSearchView(searchView: PeliasSearchView,
            autoCompleteListView: AutoCompleteListView,
            emptyView: View,
            presenter: MainPresenter,
            locationProvider: PeliasLocationProvider,
            callback: MainActivity.PeliasCallback)
    fun setSearchResultsAdapter(adapter: SearchResultsAdapter)
    fun showSearchResults()
    fun hideSearchResults()
    fun isSearchResultsVisible(): Int
    fun setCurrentItem(position: Int)
    fun getCurrentItem(): Int

    interface OnSearchResultSelectedListener {
        fun onSearchResultSelected(position: Int)
    }

    open fun enableSearch()
    open fun disableSearch()
}
