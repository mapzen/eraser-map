package com.mapzen.erasermap.view

import android.content.Context
import android.preference.PreferenceManager
import android.support.v4.view.ViewPager
import android.text.InputType
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.controller.MainActivity
import com.mapzen.erasermap.controller.MainViewController
import com.mapzen.erasermap.controller.SearchViewController
import com.mapzen.erasermap.model.AndroidAppSettings
import com.mapzen.erasermap.model.ApiKeys
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.pelias.Pelias
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.widget.AutoCompleteListView
import com.mapzen.pelias.widget.PeliasSearchView
import javax.inject.Inject

open class SearchResultsView(context: Context, attrs: AttributeSet)
        : LinearLayout(context, attrs), ViewPager.OnPageChangeListener, SearchViewController {

    override var mainController: MainViewController? = null
    override var searchView: PeliasSearchView? = null
    override var autoCompleteListView: AutoCompleteListView? = null
    override var onSearchResultsSelectedListener:
            SearchViewController.OnSearchResultSelectedListener? = null

    @Inject lateinit var pelias: Pelias
    @Inject lateinit var savedSearch: SavedSearch

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_search_results, this, true)
        orientation = LinearLayout.VERTICAL
        (context.applicationContext as EraserMapApplication).component().inject(this)
    }

    override fun initSearchView(searchView: PeliasSearchView,
            autoCompleteListView: AutoCompleteListView,
            emptyView: View,
            presenter: MainPresenter,
            locationProvider: PeliasLocationProvider,
            apiKeys: ApiKeys?,
            callback: MainActivity.PeliasCallback) {

        this.searchView = searchView
        this.autoCompleteListView = autoCompleteListView
        autoCompleteListView.hideHeader()

        searchView.setRecentSearchIconResourceId(R.drawable.ic_recent)
        searchView.setAutoCompleteIconResourceId(R.drawable.ic_pin_c)
        initAutoCompleteAdapter()
        autoCompleteListView.adapter = initAutoCompleteAdapter()
        pelias.setLocationProvider(locationProvider)
        pelias.setApiKey(apiKeys?.searchKey)
        searchView.setAutoCompleteListView(autoCompleteListView)
        searchView.setSavedSearch(savedSearch)
        searchView.setPelias(pelias)
        searchView.setCallback(callback)
        searchView.setOnSubmitListener({
            presenter.reverseGeoLngLat = null
            presenter.currentSearchTerm = searchView.query.toString()
            presenter.onQuerySubmit()
        })
        searchView.setIconifiedByDefault(false)
        searchView.imeOptions += EditorInfo.IME_FLAG_NO_EXTRACT_UI
        searchView.queryHint = context.getString(R.string.search_hint)
        autoCompleteListView.emptyView = emptyView
        restoreCurrentSearchTerm(presenter)
        searchView.setOnPeliasFocusChangeListener { view, b ->
            if (b) {
                mainController?.expandSearchView()
            } else if (presenter.resultListVisible) {
                mainController?.onCloseAllSearchResults()
                enableSearch()
            } else {
                searchView.setQuery(presenter.currentSearchTerm, false)
            }
        }
        searchView.setOnBackPressListener { mainController?.collapseSearchView() }
        val cacheSearches = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(AndroidAppSettings.KEY_CACHE_SEARCH_HISTORY, true)
        searchView.setCacheSearchResults(cacheSearches)
    }

    private fun initAutoCompleteAdapter(): SearchListViewAdapter =
            SearchListViewAdapter(context, R.layout.list_item_auto_complete,
                searchView as PeliasSearchView, savedSearch)

    private fun restoreCurrentSearchTerm(presenter: MainPresenter) {
        val term = presenter.currentSearchTerm
        if (term != null) {
            searchView?.setQuery(term, false)
            if (visibility == View.VISIBLE
                    && presenter.reverseGeo == false) {
                searchView?.clearFocus()
                mainController?.showActionViewAll()
            } else {
                mainController?.hideActionViewAll()
            }
            presenter.currentSearchTerm = null
        }
    }

    override fun setSearchResultsAdapter(adapter: SearchResultsAdapter) {
        val pager = findViewById(R.id.pager) as ViewPager
        val indicator = findViewById(R.id.indicator) as TextView
        pager.adapter = adapter
        pager.addOnPageChangeListener(this)
        indicator.text = resources.getString(R.string.search_results_indicator,
                pager.currentItem + 1, pager.adapter.count)
        setIndicatorVisibility(adapter, indicator)
    }

    private fun setIndicatorVisibility(adapter: SearchResultsAdapter, indicator: TextView) {
        if (adapter.count > 1) {
            indicator.visibility = VISIBLE
        } else {
            indicator.visibility = GONE
        }
    }

    override fun setCurrentItem(position: Int) {
        (findViewById(R.id.pager) as ViewPager).currentItem = position
    }

    override fun getCurrentItem(): Int = (findViewById(R.id.pager) as ViewPager).currentItem

    override fun showSearchResults() {
        visibility = VISIBLE
    }

    override fun hideSearchResults() {
        visibility = GONE
    }

    override fun isSearchResultsVisible(): Int {
        return visibility
    }

    override fun enableSearch() {
        searchView?.inputType = InputType.TYPE_CLASS_TEXT
        val closeButton = searchView?.findViewById(R.id.search_close_btn) as ImageView
        closeButton.visibility = View.VISIBLE
    }

    override fun disableSearch() {
        searchView?.inputType = InputType.TYPE_NULL
        val closeButton = searchView?.findViewById(R.id.search_close_btn) as ImageView
        closeButton.visibility = View.GONE
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        val pager = findViewById(R.id.pager) as ViewPager
        val indicator = findViewById(R.id.indicator) as TextView
        indicator.text = resources.getString(R.string.search_results_indicator,
                position + 1, pager.adapter.count)

        onSearchResultsSelectedListener?.onSearchResultSelected(position)
    }

    override fun onPageScrollStateChanged(state: Int) {
    }
}
