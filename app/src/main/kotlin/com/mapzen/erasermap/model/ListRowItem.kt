package com.mapzen.erasermap.model

class ListRowItem(val type: ListRowType, val layoutId: Int) {
  var iconId: Int? = null
  var title: String? = null
  var extra: Any? = null
}

enum class ListRowType {
  CURRENT_LOCATION,
  PEDESTRIAN,
  TRANSIT,
  ARRIVAL
}