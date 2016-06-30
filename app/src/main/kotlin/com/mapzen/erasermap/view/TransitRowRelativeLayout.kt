package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.mapzen.erasermap.R

class TransitRowRelativeLayout : RelativeLayout {

  constructor(context: Context) : super(context) {}
  constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {}
  constructor(context: Context, attributes: AttributeSet, style: Int) : super(context, attributes,
      style) {}

  var transitLine: View? = null

  override fun onFinishInflate() {
    super.onFinishInflate()
    transitLine = findViewById(R.id.transit_line)
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    super.onLayout(changed, l, t, r, b)
    val tl = transitLine?.left as Int
    val tt = transitLine?.top as Int
    val tr = transitLine?.right as Int
    val height = Math.abs(b - t)
    transitLine?.layout(tl, tt, tr, tt + height)
  }
}
