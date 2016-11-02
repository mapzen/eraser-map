package com.mapzen.erasermap.view

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.PermissionManager
import com.mapzen.erasermap.model.event.RoutePreviewEvent
import com.mapzen.erasermap.util.FeatureDisplayHelper
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.squareup.otto.Bus
import javax.inject.Inject

class SearchResultsAdapter(val context: Context, val features: List<Feature>,
                            val displayHelper: FeatureDisplayHelper,
                           val permissionManager: PermissionManager)
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
        title.text = displayHelper.getDisplayName(simpleFeature)
        address.text = simpleFeature.address()
        start.setOnClickListener {
            if (permissionManager.permissionsGranted()) {
                bus.post(RoutePreviewEvent(features.get(position)))
            } else {
                permissionManager.showPermissionRequired()
            }
        }
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
