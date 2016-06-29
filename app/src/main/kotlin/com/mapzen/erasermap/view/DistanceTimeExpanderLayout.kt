package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.mapzen.erasermap.R

class DistanceTimeExpanderLayout: LinearLayout {

  constructor(context: Context) : super(context) {

  }

  constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {

  }

  constructor(context: Context, attributes: AttributeSet, style: Int) : super(context, attributes,
      style) {

  }

  val openCloseArrow: ImageView by lazy { findViewById(R.id.open_close_arrow) as ImageView }

  init {
    (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
        .inflate(R.layout.distance_time_expander, this, true)
  }

  fun openArrow() {
    openCloseArrow.rotation = 180.0f
  }

  fun closeArrow() {
    openCloseArrow.rotation = 0.0f
  }
}
