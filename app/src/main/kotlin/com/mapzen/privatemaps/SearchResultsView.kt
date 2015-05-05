package com.mapzen.privatemaps

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout

public class SearchResultsView(context: Context, attrs: AttributeSet)
        : RelativeLayout(context, attrs) {

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        inflater.inflate(R.layout.view_search_results, this, true)
    }

    public fun setAdapter(adapter: PagerAdapter) {
        (findViewById(R.id.pager) as ViewPager).setAdapter(adapter)
    }
}
