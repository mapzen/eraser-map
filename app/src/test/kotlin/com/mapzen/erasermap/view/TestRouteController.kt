package com.mapzen.erasermap.view

import android.location.Location

public class TestRouteController : RouteViewController {
    public var location: Location? = null
    public var isDirectionListVisible: Boolean = false

    override fun onLocationChanged(location: Location) {
        this.location = location
    }

    override fun showDirectionList() {
        isDirectionListVisible = true
    }

    override fun hideDirectionList() {
        isDirectionListVisible = false
    }

    override fun collapseSlideLayout() {
    }
}
