package com.mapzen.erasermap.model

import com.mapzen.erasermap.view.SectionAdapterInterface
import java.util.HashMap

class SectionCalculator(val sectionInterface: SectionAdapterInterface) {

  val positionToSection: HashMap<Int, Int> by lazy { HashMap<Int, Int>() }
  val positionsToSection: HashMap<Int, Int> by lazy { HashMap<Int, Int>() }
  val positionsToRow: HashMap<Int, Int> by lazy { HashMap<Int, Int>() }
  val total: Int by lazy { calculateTotal() }


  private fun calculateTotal(): Int {
    var position = 0
    val numSections = sectionInterface.numSections()
    for (i in 0..numSections - 1 ) {
      positionToSection.put(position, i)
      positionsToSection.put(position, i)
      val numRows = sectionInterface.numRowsForSection(i)
      for (j in 0..numRows - 1) {
        position += 1
        positionsToSection.put(position, i)
        positionsToRow.put(position, j)
      }
      position += 1
    }
    return position
  }

}
