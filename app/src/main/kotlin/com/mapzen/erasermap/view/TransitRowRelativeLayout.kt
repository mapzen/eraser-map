package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import com.mapzen.erasermap.R
import java.util.ArrayList

class TransitRowRelativeLayout : DynamicChildHeightRelativeLayout {

  constructor(context: Context) : super(context) {}
  constructor(context: Context, attributes: AttributeSet) : super(context, attributes) {}
  constructor(context: Context, attributes: AttributeSet, style: Int) : super(context, attributes,
      style) {}

  override fun idsForDynamicChildren(): ArrayList<Int> {
    val children = ArrayList<Int>()
    children.add(R.id.transit_line)
    return children
  }

}
