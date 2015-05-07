package com.mapzen.privatemaps

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature

public class SearchResultsAdapter(val context: Context, val features: List<Feature>)
        : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        val simpleFeature = SimpleFeature.fromFeature(features.get(position))
        val view = View.inflate(context, R.layout.pager_item_search_results, null)
        val title = view.findViewById(R.id.title) as TextView
        val address = view.findViewById(R.id.address) as TextView
        title.setText(simpleFeature.getTitle())
        address.setText(simpleFeature.getAddress())
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
