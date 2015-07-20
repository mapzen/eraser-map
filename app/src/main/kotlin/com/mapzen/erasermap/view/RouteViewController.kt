package com.mapzen.erasermap.view

import android.location.Location

public interface RouteViewController {
    public fun onLocationChanged(location: Location)
    public fun showDirectionList()
    public fun hideDirectionList()
}
