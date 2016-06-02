package com.mapzen.erasermap.view

import android.location.Location
import com.mapzen.helpers.RouteEngine
import com.mapzen.model.ValhallaLocation

public interface RouteViewController {
    public fun onLocationChanged(location: Location)
    public fun showResumeButton()
    public fun hideResumeButton()
    public fun isResumeButtonHidden(): Boolean
    public fun showRouteIcon(location: ValhallaLocation)
    public fun centerMapOnCurrentLocation()
    public fun centerMapOnLocation(location: ValhallaLocation)
    public fun updateSnapLocation(location: ValhallaLocation)
    public fun setCurrentInstruction(index: Int)
    public fun setMilestone(index: Int, milestone: RouteEngine.Milestone)
    public fun playStartInstructionAlert()
    public fun playPreInstructionAlert(index: Int)
    public fun playPostInstructionAlert(index: Int)
    public fun updateDistanceToNextInstruction(meters: Int)
    public fun updateDistanceToDestination(meters: Int)
    public fun setRouteComplete()
    public fun showReroute(location: ValhallaLocation)
    public fun hideRouteIcon()
    public fun hideRouteLine()
    public fun showRouteDirectionList()
    public fun hideRouteDirectionList()
    public fun updateMapZoom(zoom: Float)
    public fun displayInstruction(index: Int)
}
