package com.mapzen.erasermap.view

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.mapzen.erasermap.R

/**
 * When a reroute is issued, the normal instruction adapter will be replaced with this one to
 * populate the instruction Pager with indicating reroute
 */
public class ReroutingAdapter(val context: Context) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup?, position: Int): Any? {
        val view = View.inflate(context, R.layout.pager_item_rerouting, null)
        container?.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup?, position: Int, `object`: Any?) {
        container?.removeView(`object` as View)
    }

    override fun getCount(): Int {
        return 1
    }

    override fun isViewFromObject(view: View?, obj: Any?): Boolean {
        return view == `obj`
    }
}
