package com.mapzen.erasermap.presenter

open class ViewStateManager {
    enum class ViewState {
        DEFAULT,
        SEARCH,
        SEARCH_RESULTS,
        ROUTE_PREVIEW,
        ROUTE_PREVIEW_LIST,
        ROUTING,
        ROUTE_DIRECTION_LIST
    }

    var viewState: ViewState = ViewState.DEFAULT
}
