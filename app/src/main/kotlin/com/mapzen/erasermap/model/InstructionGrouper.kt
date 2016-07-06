package com.mapzen.erasermap.model

import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import java.util.ArrayList
import java.util.HashSet

/**
 * Handles breaking the instructions up into groups so that they can be properly
 * displayed by [RoutingDirectionListAdapter]. The grouper creates a new group
 * when the next instruction's [travelType] or [travelMode] differs from the current.
 * It also creates a new group for every [TravelMode.TRANSIT].
 *
 * We dont show some instruction types which have distance, those are filtered out and
 * not added to groups
 */
class InstructionGrouper(val instructions: ArrayList<Instruction>) {

  companion object {
    const val INSTRUCTION_TYPE_TRANSIT_CONNECTION_START = 33
    const val INSTRUCTION_TYPE_TRANSIT_CONNECTION_DESTINATION = 35
  }

  private val instructionTypesNotShown = HashSet<Int>()

  lateinit var instructionGroups: ArrayList<InstructionGroup>

  init {
    instructionTypesNotShown.add(INSTRUCTION_TYPE_TRANSIT_CONNECTION_START)
    instructionTypesNotShown.add(INSTRUCTION_TYPE_TRANSIT_CONNECTION_DESTINATION)

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
      if (currType != travelType || currMode != travelMode || currMode == TravelMode.TRANSIT) {
        if ((currInstructions as ArrayList<Instruction>).size > 0) {
          groups.add(InstructionGroup(currType, currMode as TravelMode,
              currInstructions))
        }
        currType = travelType
        currMode = travelMode
        currInstructions = ArrayList<Instruction>()
        if (!instructionTypesNotShown.contains(instructions[i].turnInstruction)) {
          currInstructions.add(instructions[i])
        }
      } else {
        if (!instructionTypesNotShown.contains(instructions[i].turnInstruction)) {
          currInstructions?.add(instructions[i])
        }
      }

    }
    if ((currInstructions as ArrayList<Instruction>).size > 0) {
      groups.add(InstructionGroup(currType as TravelType, currMode as TravelMode,
          currInstructions))
    }

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
