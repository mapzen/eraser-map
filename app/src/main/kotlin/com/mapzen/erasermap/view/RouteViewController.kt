package com.mapzen.erasermap.view

import android.location.Location
import com.mapzen.helpers.RouteEngine

public interface RouteViewController {
    public fun onLocationChanged(location: Location)
    public fun showResumeButton()
    public fun hideResumeButton()
    public fun isResumeButtonHidden(): Boolean
    public fun showRouteIcon(location: Location)
    public fun centerMapOnCurrentLocation()
    public fun centerMapOnLocation(location: Location)
    public fun updateSnapLocation(location: Location)
    public fun setCurrentInstruction(index: Int)
    public fun setMilestone(index: Int, milestone: RouteEngine.Milestone)
    public fun playStartInstructionAlert()
    public fun playPreInstructionAlert(index: Int)
    public fun playPostInstructionAlert(index: Int)
    public fun updateDistanceToNextInstruction(meters: Int)
    public fun updateDistanceToDestination(meters: Int)
    public fun showRouteComplete()
    public fun showReroute(location: Location)
    public fun hideRouteIcon()
    public fun hideRouteLine()
    public fun showRouteDirectionList()
    public fun hideRouteDirectionList()
    public fun updateMapZoom(zoom: Float)
}
