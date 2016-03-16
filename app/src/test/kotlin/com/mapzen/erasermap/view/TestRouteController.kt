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
    public var alert: Int = -1
    public var pre: Int = -1
    public var post: Int = -1
    public var mapZoom: Float = 0f
    public var displayIndex: Int = 0

    override fun onLocationChanged(location: Location) {
        this.location = location
    }

    override fun showResumeButton() {
        isResumeButtonVisible = true
    }

    override fun hideResumeButton() {
        isResumeButtonVisible = false
    }

    override fun isResumeButtonHidden(): Boolean {
        return isResumeButtonVisible == false
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
        alert = index
    }

    override fun playPreInstructionAlert(index: Int) {
        pre = index
    }

    override fun playPostInstructionAlert(index: Int) {
        post = index
    }

    override fun updateDistanceToNextInstruction(meters: Int) {
    }

    override fun updateDistanceToDestination(meters: Int) {
    }

    override fun setRouteComplete() {
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

    override fun playStartInstructionAlert() {
    }

    override fun showRouteDirectionList() {
        isDirectionListVisible = true
    }

    override fun hideRouteDirectionList() {
        isDirectionListVisible = false
    }

    override fun updateMapZoom(zoom: Float) {
        mapZoom = zoom
    }

    override fun displayInstruction(index: Int) {
        displayIndex = index
    }
}
