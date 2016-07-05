package com.mapzen.erasermap.view

import android.view.View
import java.util.HashMap
import java.util.HashSet

class ViewRecycler {

  var views = HashMap<Int, HashSet<View>>()

  /**
   * Add a view to existing pool of recycled views
   */
  fun queueView(view: View, type: Int) {
    var existing = views[type]
    if (existing == null) {
      existing = HashSet<View>()
      views.put(type, existing)
    }
    existing.add(view)
  }

  /**
   * Get a view from pool of recycled views
   */
  fun dequeueView(type: Int): View? {
    val existing = views[type]
    if (existing != null && !existing.isEmpty()) {
      val view = existing.iterator().next()
      existing.remove(view)
      return view
    }
    return null
  }
}
