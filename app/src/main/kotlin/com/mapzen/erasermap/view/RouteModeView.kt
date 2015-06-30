package com.mapzen.erasermap.view

import android.content.Context
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature
import com.mapzen.valhalla.Route

public class RouteModeView : LinearLayout , ViewPager.OnPageChangeListener{
    var pager : ViewPager? = null
    var autoPage : Boolean = true
    var pagerPositionWhenPaused : Int? = 0

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if(pager?.getCurrentItem() == pagerPositionWhenPaused) {
            setCurrentPagerItemStyling(pagerPositionWhenPaused?.toInt() as Int);
            if(!autoPage) {
                resumeAutoPaging()
            }
        } else {
            setCurrentPagerItemStyling(position)
            autoPage = false
        }
    }


    override fun onPageSelected(position: Int) {
        setCurrentPagerItemStyling(pagerPositionWhenPaused?.toInt() as Int);
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    public fun setAdapter(adapter: PagerAdapter) {
        pager  = findViewById(R.id.instruction_pager) as ViewPager;
        pager?.setAdapter(adapter)
        pager?.setOnPageChangeListener(this)
    }

    public fun pageForward(position: Int) {
        pager?.setCurrentItem(position + 1)
    }

    public fun pageBackwards(position: Int) {
        pager?.setCurrentItem(position - 1)
    }

    private fun turnAutoPageOff() : Boolean {
        if (autoPage) {
            pagerPositionWhenPaused = pager?.getCurrentItem()
        }
        autoPage = false;
        return false;
    }

    private fun resumeAutoPaging() {
        pager?.setCurrentItem(pagerPositionWhenPaused?.toInt() as Int)
        setCurrentPagerItemStyling(pagerPositionWhenPaused?.toInt() as Int)
        autoPage = true
    }

    private fun setCurrentPagerItemStyling(position : Int) {
        var lastItemIndex = (pager?.getAdapter() as InstructionAdapter).getCount()?.minus(1)
        if(autoPage) {
            (pager?.getAdapter() as InstructionAdapter).setBackgroundColorActive(pager?.findViewWithTag("Instruction_" + position))
        } else {
            if(position == lastItemIndex) {
                (pager?.getAdapter() as InstructionAdapter).setBackgroundColorArrived(pager?.findViewWithTag("Instruction_" + position))
            } else {
                (pager?.getAdapter() as InstructionAdapter).setBackgroundColorInactive(pager?.findViewWithTag("Instruction_" + position))
            }
        }
    }

    public constructor(context: Context) : super(context) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
    : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.view_route_mode, this, true)
    }
}
