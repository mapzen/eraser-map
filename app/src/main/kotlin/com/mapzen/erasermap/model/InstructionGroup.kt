package com.mapzen.erasermap.model

import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import java.util.ArrayList


class InstructionGroup(val travelType: TravelType, val travelMode: TravelMode,
    val instructions: ArrayList<Instruction>) {

  val totalDistance: Int by lazy { calculateTotalDistance() }
  val totalTime: Int by lazy { calculateTotalTime() }

  private fun calculateTotalDistance(): Int {
    var total = 0
    for(i in 0..instructions.size - 1) {
      total+= instructions[i].distance
    }
    return total
  }

  private fun calculateTotalTime(): Int {
    var total = 0
    for(i in 0..instructions.size - 1) {
      total+= instructions[i].getTime()
    }
    return total
  }
}