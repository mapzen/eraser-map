package com.mapzen.erasermap.model

import com.mapzen.erasermap.TestUtils
import com.mapzen.valhalla.Instruction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test
import java.util.ArrayList

class InstructionGrouperTest {

  lateinit var grouper: InstructionGrouper

  @Before fun setup() {
    val instructions = ArrayList<Instruction>()
    instructions.add(TestUtils.getInstruction("0"))
    instructions.add(TestUtils.getInstruction("1"))
    instructions.add(TestUtils.getInstruction("2"))
    instructions.add(TestUtils.getInstruction("3"))
    instructions.add(TestUtils.getInstruction("3"))
    instructions.add(TestUtils.getInstruction("4"))
    instructions.add(TestUtils.getInstruction("5"))
    instructions.add(TestUtils.getInstruction("6"))
    grouper = InstructionGrouper(instructions)
  }

  @Test fun numGroups_shouldBeThree() {
    assertThat(grouper.numGroups()).isEqualTo(4)
  }

  @Test fun numInstructionsInGroup_shouldBeOneTwoThree() {
    assertThat(grouper.numInstructionsInGroup(0)).isEqualTo(3)
    assertThat(grouper.numInstructionsInGroup(1)).isEqualTo(1)
    assertThat(grouper.numInstructionsInGroup(2)).isEqualTo(1)
    assertThat(grouper.numInstructionsInGroup(3)).isEqualTo(3)
  }

  @Test fun getInstructionGroup_shouldReturnCorrectGroup() {
    assertThat(grouper.getInstructionGroup(0).instructions[0].getHumanTurnInstruction()).isEqualTo(
        "Instruction 0")
    assertThat(grouper.getInstructionGroup(1).instructions[0].getHumanTurnInstruction()).isEqualTo(
        "Instruction 3")
    assertThat(grouper.getInstructionGroup(2).instructions[0].getHumanTurnInstruction()).isEqualTo(
        "Instruction 3")
    assertThat(grouper.getInstructionGroup(3).instructions[0].getHumanTurnInstruction()).isEqualTo(
        "Instruction 4")
  }

}