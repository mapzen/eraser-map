package com.mapzen.erasermap.view

import android.view.View
import com.mapzen.erasermap.EraserMapApplication
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ViewRecyclerTest {

  val context = EraserMapApplication()
  val recycler = ViewRecycler()
  
  @Test fun shouldQueueViews() {
    recycler.queueView(View(context), 1)
    recycler.queueView(View(context), 1)
    recycler.queueView(View(context), 1)
    assertThat(recycler.views[1]?.size).isEqualTo(3)
  }

  @Test fun shouldDequeueViews() {
    recycler.queueView(View(context), 1)
    assertThat(recycler.dequeueView(1)).isNotNull()
    assertThat(recycler.dequeueView(1)).isNull()
    recycler.queueView(View(context), 1)
    recycler.queueView(View(context), 1)
    assertThat(recycler.dequeueView(2)).isNull()
    assertThat(recycler.dequeueView(1)).isNotNull()
    assertThat(recycler.dequeueView(1)).isNotNull()
    assertThat(recycler.dequeueView(1)).isNull()
  }

  @Test fun shouldReturnCorrectType() {
    val view = View(context)
    val view2 = View(context)
    recycler.queueView(view, 1)
    recycler.queueView(view2, 2)
    assertThat(recycler.dequeueView(1)).isEqualTo(view)
    assertThat(recycler.dequeueView(2)).isEqualTo(view2)
  }
}
