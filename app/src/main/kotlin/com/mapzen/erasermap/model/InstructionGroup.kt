package com.mapzen.erasermap.model

import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import java.util.ArrayList


class InstructionGroup(val strings: ArrayList<String>,
    val types: ArrayList<Int>, val distances: ArrayList<Int>, val times: ArrayList<Int>,
    val travelType: TravelType, val travelMode: TravelMode) {

  val totalDistance: Int by lazy { calculateTotalDistance() }
  val totalTime: Int by lazy { calculateTotalTime() }

  private fun calculateTotalDistance(): Int {
    var total = 0
    for(i in 0..distances.size - 1) {
      total+= distances[i]
    }
    return total
  }

  private fun calculateTotalTime(): Int {
    var total = 0
    for(i in 0..times.size - 1) {
      total+= times[i]
    }
    return total
  }
}