package com.mapzen.erasermap.view

import android.content.Context
import android.location.Location
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import com.mapzen.erasermap.PrivateMapsApplication
import com.mapzen.erasermap.R
import com.mapzen.helpers.DistanceFormatter
import com.mapzen.helpers.RouteEngine
import com.mapzen.valhalla.Route
import java.util.ArrayList
import javax.inject.Inject

public class RouteModeView : LinearLayout , ViewPager.OnPageChangeListener{
    var pager: ViewPager? = null
    var autoPage: Boolean = true
    var pagerPositionWhenPaused: Int? = 0
    var route: Route? = null
    var routeEngine: RouteEngine? = null
    @Inject set

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
        pager?.addOnPageChangeListener(this)
        (findViewById(R.id.destination_distance) as TextView).setText(DistanceFormatter.format(route?.getRemainingDistanceToDestination() as Int))
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
        var lastItemIndex = (pager?.getAdapter() as InstructionAdapter).getCount() - 1
        var itemsUntilLastInstruction = (lastItemIndex - position)
        if(itemsUntilLastInstruction ==  1) {
            (pager?.getAdapter() as InstructionAdapter).setBackgroundColorArrived(pager?.findViewWithTag("Instruction_" + (position + 1)))
        }
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
        init(context)
    }

    public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    public constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
    : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        (context.getApplicationContext() as PrivateMapsApplication).component()?.inject(this)
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.view_route_mode, this, true)
        routeEngine?.setListener(RouteModeListener())
    }

    inner class RouteModeListener : RouteEngine.RouteListener {
        private val TAG: String = "RouteListener"
        public var debug: Boolean = true

        override fun onSnapLocation(originalLocation: Location?, snapLocation: Location?) {
            log("[onSnapLocation]", "originalLocation = " + originalLocation
                    + " | " + "snapLocation = " + snapLocation)
        }

        override fun onUpdateDistance(distanceToNextInstruction: Int, distanceToDestination: Int) {
            log("[onUpdateDistance]", "distanceToNextInstruction = " + distanceToNextInstruction
                    + " | " + "distanceToDestination = " + distanceToDestination)
        }

        override fun onInstructionComplete(index: Int) {
            log("[onInstructionComplete]", index)
        }

        override fun onRecalculate(location: Location?) {
            log("[onRecalculate]", location)
        }

        override fun onApproachInstruction(index: Int) {
            log("[onApproachInstruction]", index)
        }

        override fun onRouteComplete() {
            log("[onRouteComplete]")
        }

        private fun log(method: String, message: Any? = null) {
            if (debug) {
                var output = String()
                output += method
                if (message != null) {
                    output += " " + message
                }

                Log.d(TAG, output)
            }
        }
    }

    fun onLocationChanged(location: Location) {
        if (route != null) {
            routeEngine?.onLocationChanged(location)
        }
    }
}
