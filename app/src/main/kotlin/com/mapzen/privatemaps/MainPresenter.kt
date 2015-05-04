package com.mapzen.privatemaps

import com.mapzen.pelias.gson.Result
import com.mapzen.pelias.widget.PeliasSearchView

public trait MainPresenter {
    public var currentSearchTerm: String?
    public var viewController: ViewController?

    public fun onSearchResultsAvailable(result: Result?)
    public fun restoreViewState()
    public fun onCollapseSearchView()
    public fun onQuerySubmit()
}
