package com.mapzen.erasermap.view

import android.view.View
import android.view.ViewGroup

interface SectionViewAdapterInterface {
  abstract fun viewForSection(section: Int, convertView: View?, parent: ViewGroup?): View
  abstract fun viewForRow(position: Int, section: Int, row: Int, convertView: View?,
      parent: ViewGroup?): View
}
