package com.mapzen.erasermap.model

import com.mapzen.valhalla.TravelMode

enum class ListRowType {
  CURRENT_LOCATION,
  PEDESTRIAN,
  TRANSIT,
  ARRIVAL
}

class ListRowItem(val type: ListRowType, val layoutId: Int) {
  var iconId: Int? = null
  var title: String? = null
  var extra: Any? = null
  var expanded: Boolean
  var nextItemMode: TravelMode? = null
  var prevMode: TravelMode? = null

  init {
    expanded = (type == ListRowType.PEDESTRIAN)
  }
}