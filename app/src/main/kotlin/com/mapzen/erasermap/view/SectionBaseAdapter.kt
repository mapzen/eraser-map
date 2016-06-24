package com.mapzen.erasermap.view

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.mapzen.erasermap.model.SectionCalculator

open abstract class SectionBaseAdapter() : BaseAdapter(), SectionAdapterInterface {

  lateinit var sectionCalculator: SectionCalculator

  init {
    sectionCalculator = SectionCalculator(this)
  }

  abstract fun viewForSection(section: Int, convertView: View?, parent: ViewGroup?): View

  abstract fun viewForRow(position: Int, section: Int, row: Int, convertView: View?,
      parent: ViewGroup?): View

  override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
    if (sectionCalculator.positionToSection.containsKey(position)) {
      return viewForSection(sectionCalculator.positionToSection[position] as Int, convertView,
          parent)
    } else {
      return viewForRow(position, sectionCalculator.positionsToSection[position] as Int,
          sectionCalculator.positionsToRow[position] as Int, convertView, parent)
    }

  }

  override fun getItemId(position: Int): Long {
    return 0
  }

  override fun getItem(position: Int): Any? {
    return 0
  }

  override fun getItemViewType(position: Int): Int {
    if (sectionCalculator.positionToSection.contains(position)) {
      return 0
    }
    return 1
  }

  override fun getViewTypeCount(): Int {
    return 2
  }

  override fun getCount(): Int {
    return sectionCalculator.total
  }


}
