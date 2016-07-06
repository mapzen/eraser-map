package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.mapzen.erasermap.R
import java.util.ArrayList

class PedestrianRowRelativeLayout : DynamicChildHeightRelativeLayout {

  constructor(context: Context) : super(context) {}
  constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {}
  constructor(context: Context, attributes: AttributeSet, style: Int) : super(context, attributes,
      style) {}

  override fun idsForDynamicChildren(): ArrayList<Int> {
    val children = ArrayList<Int>()
    children.add(R.id.dashed_line_container)
    children.add(R.id.dashed_line)
    return children
  }
}
