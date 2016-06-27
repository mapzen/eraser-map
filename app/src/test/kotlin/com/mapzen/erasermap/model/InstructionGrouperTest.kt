package com.mapzen.erasermap.model

import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

class InstructionGrouperTest {

  lateinit var grouper: InstructionGrouper

  @Before fun setup() {
    val strings = ArrayList<String>()
    strings.add("Instruction 1")
    strings.add("Instruction 2")
    strings.add("Instruction 3")
    strings.add("Instruction 4")
    strings.add("Instruction 5")
    strings.add("Instruction 6")

    val types = ArrayList<Int>()
    types.add(1)
    types.add(2)
    types.add(3)
    types.add(4)
    types.add(5)
    types.add(6)

    val distances = ArrayList<Int>()
    distances.add(10)
    distances.add(20)
    distances.add(30)
    distances.add(40)
    distances.add(50)
    distances.add(60)

    val times = ArrayList<Int>()
    times.add(10)
    times.add(20)
    times.add(30)
    times.add(40)
    times.add(50)
    times.add(60)

    val travelTypes = ArrayList<TravelType>()
    travelTypes.add(TravelType.FOOT)
    travelTypes.add(TravelType.BUS)
    travelTypes.add(TravelType.BUS)
    travelTypes.add(TravelType.FOOT)
    travelTypes.add(TravelType.FOOT)
    travelTypes.add(TravelType.FOOT)

    val travelModes = ArrayList<TravelMode>()
    travelModes.add(TravelMode.PEDESTRIAN)
    travelModes.add(TravelMode.TRANSIT)
    travelModes.add(TravelMode.TRANSIT)
    travelModes.add(TravelMode.PEDESTRIAN)
    travelModes.add(TravelMode.PEDESTRIAN)
    travelModes.add(TravelMode.PEDESTRIAN)

    grouper = InstructionGrouper(strings, types, distances, times, travelTypes, travelModes)
  }

  @Test fun numGroups_shouldBeThree() {
    assertThat(grouper.numGroups()).isEqualTo(3)
  }

  @Test fun numInstructionsInGroup_shouldBeOneTwoThree() {
    assertThat(grouper.numInstructionsInGroup(0)).isEqualTo(1)
    assertThat(grouper.numInstructionsInGroup(1)).isEqualTo(2)
    assertThat(grouper.numInstructionsInGroup(2)).isEqualTo(3)
  }

  @Test fun getInstructionGroup_shouldReturnCorrectGroup() {
    assertThat(grouper.getInstructionGroup(0).distances[0]).isEqualTo(10)
    assertThat(grouper.getInstructionGroup(1).distances[0]).isEqualTo(20)
    assertThat(grouper.getInstructionGroup(2).distances[0]).isEqualTo(40)
  }

}