package com.mapzen.erasermap.presenter

public open class ViewStateManager {
    enum class ViewState {
        DEFAULT,
        SEARCH,
        SEARCH_RESULTS,
        ROUTE_PREVIEW,
        ROUTING,
        ROUTE_DIRECTION_LIST
    }

    public var viewState: ViewState = ViewState.DEFAULT
}
