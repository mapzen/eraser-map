package com.mapzen.erasermap.model

import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import java.util.ArrayList

class InstructionGrouper(val strings: ArrayList<String>,
    val types: ArrayList<Int>, val distances: ArrayList<Int>,
    val travelTypes: ArrayList<TravelType>, val travelModes: ArrayList<TravelMode>) {

  lateinit var instructionGroups: ArrayList<InstructionGroup>

  init {
    val groups = ArrayList<InstructionGroup>()

    var currType: TravelType? = null
    var currStrings: ArrayList<String>? = null
    var currTypes: ArrayList<Int>? = null
    var currDistances: ArrayList<Int>? = null
    var currMode: TravelMode? = null

    for (i in 0..travelTypes.size-1) {
      val travelType = travelTypes[i]
      if (currType == null) {
        currType = travelType
        currStrings = ArrayList<String>()
        currTypes = ArrayList<Int>()
        currDistances = ArrayList<Int>()
        currMode = travelModes[i]
      }
      if (currType != travelType) {
        groups.add(InstructionGroup(currStrings as ArrayList<String>,
            currTypes as ArrayList<Int>, currDistances as ArrayList<Int>,
            currType , currMode as TravelMode))
        currType = travelType
        currStrings = ArrayList<String>()
        currTypes = ArrayList<Int>()
        currDistances = ArrayList<Int>()
        currMode = travelModes[i]
        currStrings?.add(strings[i])
        currTypes?.add(types[i])
        currDistances?.add(distances[i])
      } else {
        currStrings?.add(strings[i])
        currTypes?.add(types[i])
        currDistances?.add(distances[i])
      }

    }
    groups.add(InstructionGroup(currStrings as ArrayList<String>,
        currTypes as ArrayList<Int>, currDistances as ArrayList<Int>,
        currType as TravelType , currMode as TravelMode))

    instructionGroups = groups
  }

  fun numGroups(): Int {
    return instructionGroups.size
  }

  fun numInstructionsInGroup(index: Int): Int {
    return instructionGroups[index].strings.size
  }

  fun getInstructionGroup(index: Int): InstructionGroup {
    return instructionGroups[index]
  }

}