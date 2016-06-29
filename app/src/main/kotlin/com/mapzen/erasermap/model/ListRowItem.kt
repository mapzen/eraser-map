package com.mapzen.erasermap.model

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

  init {
    expanded = (type == ListRowType.PEDESTRIAN)
  }
}