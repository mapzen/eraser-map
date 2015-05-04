package com.mapzen.privatemaps

import com.mapzen.pelias.gson.Result

public trait MainPresenter {
    public var currentSearchTerm: String?
    public var viewController: ViewController?

    public fun onSearchResultsAvailable(result: Result?)
    public fun restoreViewState()
}
