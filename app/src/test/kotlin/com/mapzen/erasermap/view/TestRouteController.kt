package com.mapzen.erasermap.view

import android.location.Location
import com.mapzen.helpers.RouteEngine

public class TestRouteController : RouteViewController {
    public var location: Location? = null
    public var mapLocation: Location? = null
    public var isDirectionListVisible: Boolean = false
    public var isResumeButtonVisible: Boolean = false
    public var isRouteIconVisible: Boolean = false
    public var isRouteLineVisible: Boolean = false

    override fun onLocationChanged(location: Location) {
        this.location = location
    }

    override fun collapseSlideLayout() {
    }

    override fun showDirectionList() {
        isDirectionListVisible = true
    }

    override fun hideDirectionList() {
        isDirectionListVisible = false
    }

    override fun showResumeButton() {
        isResumeButtonVisible = true
    }

    override fun hideResumeButton() {
        isResumeButtonVisible = false
    }

    override fun showRouteIcon(location: Location) {
    }

    override fun centerMapOnCurrentLocation() {
    }

    override fun centerMapOnLocation(location: Location) {
        mapLocation = location
    }

    override fun setCurrentInstruction(index: Int) {
    }

    override fun setMilestone(index: Int, milestone: RouteEngine.Milestone) {
    }

    override fun playPreInstructionAlert(index: Int) {
    }

    override fun playPostInstructionAlert(index: Int) {
    }

    override fun updateDistanceToNextInstruction(meters: Int) {
    }

    override fun updateDistanceToDestination(meters: Int) {
    }

    override fun showRouteComplete() {
    }

    override fun showReroute(location: Location) {
    }

    override fun updateSnapLocation(location: Location) {
    }

    override fun hideRouteIcon() {
        isRouteIconVisible = false
    }

    override fun hideRouteLine() {
        isRouteLineVisible = false
    }
}
