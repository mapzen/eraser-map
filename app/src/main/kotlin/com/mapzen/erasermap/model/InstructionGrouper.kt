package com.mapzen.erasermap.model

import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import java.util.ArrayList

class InstructionGrouper(val instructions: ArrayList<Instruction>) {

  lateinit var instructionGroups: ArrayList<InstructionGroup>

  init {
    val groups = ArrayList<InstructionGroup>()

    var currInstructions: ArrayList<Instruction>? = null
    var currType: TravelType? = null
    var currMode: TravelMode? = null

    for (i in 0..instructions.size-1) {
      val travelType = instructions[i].getTravelType()
      val travelMode = instructions[i].getTravelMode()
      if (currType == null) {
        currType = travelType
        currInstructions = ArrayList<Instruction>()
        currMode = travelMode
      }
      if (currType != travelType || currMode != travelMode) {
        groups.add(InstructionGroup(currType , currMode as TravelMode, currInstructions as ArrayList<Instruction>))
        currType = travelType
        currMode = travelMode
        currInstructions = ArrayList<Instruction>()
        currInstructions.add(instructions[i])
      } else {
        currInstructions?.add(instructions[i])
      }

    }
    groups.add(InstructionGroup(currType as TravelType , currMode as TravelMode, currInstructions as ArrayList<Instruction>))

    instructionGroups = groups
  }

  fun numGroups(): Int {
    return instructionGroups.size
  }

  fun numInstructionsInGroup(index: Int): Int {
    return instructionGroups[index].instructions.size
  }

  fun getInstructionGroup(index: Int): InstructionGroup {
    return instructionGroups[index]
  }

}