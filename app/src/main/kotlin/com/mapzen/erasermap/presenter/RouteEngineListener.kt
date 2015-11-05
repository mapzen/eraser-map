package com.mapzen.erasermap.presenter

import android.location.Location
import android.util.Log
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.helpers.RouteEngine
import com.mapzen.helpers.RouteListener

class RouteEngineListener : RouteListener {
    companion object {
        val TAG = "RouteEngineListener"
    }

    public var controller: RouteViewController? = null
    public var debug: Boolean = true

    override fun onSnapLocation(originalLocation: Location, snapLocation: Location) {
        log("[onSnapLocation]", "original = $originalLocation | snap = $snapLocation")
        controller?.updateSnapLocation(snapLocation);
    }

    override fun onMilestoneReached(index: Int, milestone: RouteEngine.Milestone) {
        log("[onApproachInstruction]", index)
        controller?.setCurrentInstruction(index)
        controller?.setMilestone(index, milestone)
    }

    override fun onApproachInstruction(index: Int) {
        log("[onAlertInstruction]", index)
        controller?.setCurrentInstruction(index)
        controller?.playPreInstructionAlert(index)
    }

    override fun onInstructionComplete(index: Int) {
        log("[onInstructionComplete]", index)
        controller?.playPostInstructionAlert(index)
    }

    override fun onUpdateDistance(next: Int, destination: Int) {
        log("[onUpdateDistance]", "next = $next | destination = $destination")
        controller?.updateDistanceToNextInstruction(next)
        controller?.updateDistanceToDestination(destination)
    }

    override fun onRouteComplete() {
        log("[onRouteComplete]")
        controller?.showRouteComplete()
    }

    override fun onRecalculate(location: Location) {
        log("[onRecalculate]", location)
        controller?.showReroute(location)
    }

    private fun log(method: String, message: Any? = null) {
        if (debug) {
            var output = String()
            output += method
            if (message != null) {
                output += " " + message
            }

            Log.d(TAG, output)
        }
    }
}
