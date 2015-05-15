package com.mapzen.erasermap.view

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.mapzen.erasermap.PrivateMapsApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.RoutePreviewEvent
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.squareup.otto.Bus
import javax.inject.Inject

public class SearchResultsAdapter(val context: Context, val features: List<Feature>)
        : PagerAdapter() {

    var bus: Bus? = null
        [Inject] set

    init {
        (context.getApplicationContext() as PrivateMapsApplication).component().inject(this)
    }

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        val simpleFeature = SimpleFeature.fromFeature(features.get(position))
        val view = View.inflate(context, R.layout.pager_item_search_results, null)
        val title = view.findViewById(R.id.title) as TextView
        val address = view.findViewById(R.id.address) as TextView
        val start = view.findViewById(R.id.preview) as ImageButton
        title.setText(simpleFeature.getTitle())
        address.setText(simpleFeature.getAddress())
        start.setOnClickListener { bus?.post(RoutePreviewEvent(features.get(position))) }
        container?.addView(view)
        return view
    }

    override fun getCount(): Int {
        return features.size()
    }

    override fun isViewFromObject(view: View?, `object`: Any?): Boolean {
        return view == `object`
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as View)
    }
}
