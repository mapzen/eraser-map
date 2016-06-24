package com.mapzen.erasermap.view

import android.view.View
import android.view.ViewGroup

interface SectionAdapterInterface {
  abstract fun numSections(): Int
  abstract fun numRowsForSection(section: Int): Int
}
