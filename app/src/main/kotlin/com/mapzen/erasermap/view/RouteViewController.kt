package com.mapzen.erasermap.view

import android.location.Location

public interface RouteViewController {
    public fun onLocationChanged(location: Location)
    public fun collapseSlideLayout()
    public fun showDirectionList()
    public fun hideDirectionList()
    public fun showResumeButton()
    public fun hideResumeButton()
    public fun centerMapOnLocation(location: Location, rotation: Float)
}
