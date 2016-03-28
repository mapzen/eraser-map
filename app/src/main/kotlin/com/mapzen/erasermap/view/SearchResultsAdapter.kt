package com.mapzen.erasermap.view

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.controller.MainActivity.Companion.CONFIDENCE_THRESHOLD
import com.mapzen.erasermap.model.event.RoutePreviewEvent
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.squareup.otto.Bus
import javax.inject.Inject

class SearchResultsAdapter(val context: Context, val features: List<Feature>, val reverseGeo: Boolean)
        : PagerAdapter() {

    @Inject lateinit var bus: Bus

    init {
        (context.applicationContext as EraserMapApplication).component()
                .inject(this@SearchResultsAdapter)
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        val simpleFeature = SimpleFeature.fromFeature(features.get(position))
        val view = View.inflate(context, R.layout.pager_item_search_results, null)
        val title = view.findViewById(R.id.title) as TextView
        val address = view.findViewById(R.id.address) as TextView
        val start = view.findViewById(R.id.preview) as ImageButton
        val confidence = simpleFeature.confidence()
        if (reverseGeo && confidence < CONFIDENCE_THRESHOLD) {
            title.text = String.format(context.getString(R.string.near_feature), simpleFeature.name())
        } else {
            title.text = simpleFeature.name()
        }
        address.text = simpleFeature.address()
        start.setOnClickListener { bus.post(RoutePreviewEvent(features.get(position))) }
        container?.addView(view)
        return view
    }

    override fun getCount(): Int {
        return features.size
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as View)
    }
}
