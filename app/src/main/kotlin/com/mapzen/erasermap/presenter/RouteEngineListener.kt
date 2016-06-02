package com.mapzen.erasermap.presenter

import android.util.Log
import com.mapzen.erasermap.view.RouteViewController
import com.mapzen.helpers.RouteListener
import com.mapzen.helpers.RouteEngine
import com.mapzen.model.ValhallaLocation

class RouteEngineListener : RouteListener {
    companion object {
        val TAG = "RouteEngineListener"
    }

    public var controller: RouteViewController? = null
    public var debug: Boolean = true

    override fun onRouteStart() {
        log("[onRouteStart]")
        controller?.setCurrentInstruction(0)
        controller?.playPreInstructionAlert(0)
    }

    override fun onSnapLocation(originalLocation: ValhallaLocation, snapLocation: ValhallaLocation) {
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
        if (index != 0) {
            controller?.playPostInstructionAlert(index)
        }
    }

    override fun onUpdateDistance(next: Int, destination: Int) {
        log("[onUpdateDistance]", "next = $next | destination = $destination")
        controller?.updateDistanceToNextInstruction(next)
        controller?.updateDistanceToDestination(destination)
    }

    override fun onRouteComplete() {
        log("[onRouteComplete]")
        controller?.setRouteComplete()
    }

    override fun onRecalculate(location: ValhallaLocation) {
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
