package com.mapzen.erasermap.model

import android.content.Context
import com.mapzen.erasermap.R
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import java.util.ArrayList


class InstructionGroup(val travelType: TravelType, val travelMode: TravelMode,
    val instructions: ArrayList<Instruction>) {

  val totalDistance: Int by lazy { calculateTotalDistance() }
  val totalTime: Int by lazy { calculateTotalTime() }
  var firstStationName: String? = null
  var numberOfStops: String? = null

  private fun calculateTotalDistance(): Int {
    var total = 0
    for(i in instructions.indices) {
      total+= instructions[i].distance
    }
    return total
  }

  private fun calculateTotalTime(): Int {
    var total = 0
    for(i in instructions.indices) {
      total+= instructions[i].getTime()
    }
    return total
  }

  fun firstStationName(context: Context, instruction: Instruction): String? {
    if (firstStationName == null) {
      val transitStop = instruction.getTransitInfo()?.getTransitStops()?.get(0)
      if (transitStop != null) {
        val builder = StringBuilder()
        builder.append(transitStop?.getName())
        builder.append(" ")
        builder.append(context.getString(R.string.station))
        firstStationName = builder.toString()
      }
    }
    return firstStationName
  }

  fun numberOfStops(context: Context, instruction: Instruction): String? {
    if (numberOfStops == null) {
      val stops = instruction.getTransitInfo()?.getTransitStops()
      if (stops != null) {
        val numStops = stops?.size - 1
        val builder = StringBuilder()
        builder.append(numStops)
        builder.append(" ")
        builder.append(context.getString(R.string.stops))
        numberOfStops = builder.toString()
      }
    }
    return numberOfStops
  }
}
