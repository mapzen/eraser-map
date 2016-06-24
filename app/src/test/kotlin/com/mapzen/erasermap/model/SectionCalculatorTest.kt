package com.mapzen.erasermap.model

import com.mapzen.erasermap.view.SectionAdapterInterface
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class SectionCalculatorTest {

  lateinit var sectionCalculator: SectionCalculator

  @Before fun setup() {
    val adapter = TestSectionAdapter()
    sectionCalculator = SectionCalculator(adapter)
    sectionCalculator.total
  }

  @Test fun positionToSection_shouldHaveCorrectValues() {
    assertThat(sectionCalculator.positionToSection.size).isEqualTo(4)
    assertThat(sectionCalculator.positionToSection[0]).isEqualTo(0)
    assertThat(sectionCalculator.positionToSection[4]).isEqualTo(1)
    assertThat(sectionCalculator.positionToSection[7]).isEqualTo(2)
    assertThat(sectionCalculator.positionToSection[9]).isEqualTo(3)
  }

  @Test fun positionsToSection_shouldHaveCorrectValues() {
    assertThat(sectionCalculator.positionsToSection.size).isEqualTo(11)
    assertThat(sectionCalculator.positionsToSection[0]).isEqualTo(0)
    assertThat(sectionCalculator.positionsToSection[1]).isEqualTo(0)
    assertThat(sectionCalculator.positionsToSection[2]).isEqualTo(0)
    assertThat(sectionCalculator.positionsToSection[3]).isEqualTo(0)
    assertThat(sectionCalculator.positionsToSection[4]).isEqualTo(1)
    assertThat(sectionCalculator.positionsToSection[5]).isEqualTo(1)
    assertThat(sectionCalculator.positionsToSection[6]).isEqualTo(1)
    assertThat(sectionCalculator.positionsToSection[7]).isEqualTo(2)
    assertThat(sectionCalculator.positionsToSection[8]).isEqualTo(2)
    assertThat(sectionCalculator.positionsToSection[9]).isEqualTo(3)
    assertThat(sectionCalculator.positionsToSection[10]).isEqualTo(3)
  }

  @Test fun positionsToRow_shouldHaveCorrectValues() {
    assertThat(sectionCalculator.positionsToRow.size).isEqualTo(7)
    assertThat(sectionCalculator.positionsToRow[1]).isEqualTo(0)
    assertThat(sectionCalculator.positionsToRow[2]).isEqualTo(1)
    assertThat(sectionCalculator.positionsToRow[3]).isEqualTo(2)
    assertThat(sectionCalculator.positionsToRow[5]).isEqualTo(0)
    assertThat(sectionCalculator.positionsToRow[6]).isEqualTo(1)
    assertThat(sectionCalculator.positionsToRow[8]).isEqualTo(0)
    assertThat(sectionCalculator.positionsToRow[10]).isEqualTo(0)
  }

  @Test fun total_shouldBeEleven() {
    assertThat(sectionCalculator.total).isEqualTo(11)
  }

  class TestSectionAdapter : SectionAdapterInterface {

    override fun numSections(): Int {
      return 4
    }

    override fun numRowsForSection(section: Int): Int {
      when(section) {
        0 -> return 3
        1 -> return 2
        2 -> return 1
        3 -> return 1
      }
      return 0
    }

  }
}
