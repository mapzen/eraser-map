package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import java.util.ArrayList

abstract class DynamicChildHeightRelativeLayout : RelativeLayout {

  constructor(context: Context) : super(context)
  constructor(context: Context, attributes: AttributeSet) : super(context, attributes)
  constructor(context: Context, attributes: AttributeSet, style: Int) : super(context, attributes,
      style)

  abstract fun idsForDynamicChildren(): ArrayList<Int>

  var views: ArrayList<View>? = null

  override fun onFinishInflate() {
    super.onFinishInflate()
    views = ArrayList<View>()
    for (i in idsForDynamicChildren().indices) {
      val viewId = idsForDynamicChildren()[i]
      (views as ArrayList<View>).add(findViewById(viewId))
    }
  }

  override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    super.onLayout(changed, l, t, r, b)
    if (views != null) {
      val height = Math.abs(b - t)
      for (i in views!!.indices) {
        val view = views!![i]
        val lp = view.layoutParams as MarginLayoutParams
        val bottomMargin = lp.bottomMargin
        layoutView(view, height - bottomMargin)
      }
    }

  }

  private fun layoutView(view: View, height: Int) {
    val tl = view.left
    val tt = view.top
    val tr = view.right
    view.layout(tl, tt, tr, height)
  }
}
