package com.mapzen.erasermap.view

import android.content.Context
import android.location.Location
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.helpers.RouteEngine
import com.mapzen.valhalla.Route
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import java.util.ArrayList
import javax.inject.Inject

public class RouteModeView : LinearLayout, RouteViewController, ViewPager.OnPageChangeListener {
    companion object {
        val VIEW_TAG: String = "Instruction_"
    }
    public val SLIDING_PANEL_OFFSET_OPEN: Float = 0.1f

    var pager: ViewPager? = null
    var autoPage: Boolean = true
    var route: Route? = null
    var slideLayout: SlidingUpPanelLayout? = null
    var panelListener: SlidingUpPanelLayout.PanelSlideListener? = null
    var routeEngine: RouteEngine? = null
    @Inject set
    var routeListener: RouteModeListener = RouteModeListener()
    var presenter: MainPresenter? = null

    private var currentInstructionIndex: Int = 0

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
        (context.getApplicationContext() as EraserMapApplication).component()?.inject(this)
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.view_route_mode, this, true)
        routeEngine?.setListener(routeListener)
        (findViewById(R.id.resume) as ImageButton).setOnClickListener {
            presenter?.onResumeRouting()
            pager?.setCurrentItem(currentInstructionIndex)
        }
        initSlideLayout(findViewById(R.id.sliding_layout))
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if(pager?.getCurrentItem() == currentInstructionIndex) {
            setCurrentPagerItemStyling(currentInstructionIndex);
            if(!autoPage) {
                resumeAutoPaging()
            }
        } else {
            setCurrentPagerItemStyling(position)
            autoPage = false
        }
    }

    override fun onPageSelected(position: Int) {
        setCurrentPagerItemStyling(currentInstructionIndex);
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    public fun setAdapter(adapter: PagerAdapter) {
        pager = findViewById(R.id.instruction_pager) as ViewPager
        pager?.setAdapter(adapter)
        pager?.addOnPageChangeListener(this)
        (findViewById(R.id.destination_distance) as DistanceView).distanceInMeters =
                (route?.getRemainingDistanceToDestination() as Int)
    }

    public fun pageForward(position: Int) {
        pager?.setCurrentItem(position + 1)
    }

    public fun pageBackwards(position: Int) {
        pager?.setCurrentItem(position - 1)
    }

    public fun initSlideLayout(view: View) {
        slideLayout = view as SlidingUpPanelLayout
        slideLayout?.setDragView(view.findViewById(R.id.drag_area))
        panelListener = getPanelSlideListener(view)
        slideLayout?.setPanelSlideListener(panelListener)
        slideLayout?.setTouchEnabled(false)
        findViewById(R.id.drag_area).setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                slideLayout?.setTouchEnabled(true)
                return true
            }
        })
    }

    public fun getPanelSlideListener(view : View):SlidingUpPanelLayout.PanelSlideListener {
        return (object:SlidingUpPanelLayout.PanelSlideListener {

            public override fun onPanelSlide(panel:View, slideOffset:Float) {
                if (slideOffset >=  SLIDING_PANEL_OFFSET_OPEN) {
                    showDirectionList(view);
                }

                if (slideOffset <  SLIDING_PANEL_OFFSET_OPEN) {
                    findViewById(R.id.footer).setVisibility(View.VISIBLE)
                    slideLayout?.setDragView(view.findViewById(R.id.drag_area))
                }

                if (slideOffset == SLIDING_PANEL_OFFSET_OPEN) {
                    slideLayout?.setTouchEnabled(false)
                }
            }

            public override fun onPanelExpanded(panel:View) { }

            public override fun onPanelCollapsed(panel: View) {
                slideLayout?.setTouchEnabled(false)
            }

            public override fun onPanelAnchored(panel:View) { }

            public override fun onPanelHidden(view:View) { }
        })
    }

    private fun showDirectionList(view : View)  {
        findViewById(R.id.footer).setVisibility(View.GONE)
        val listView = findViewById(R.id.instruction_list_view) as ListView
        val instructionStrings = ArrayList<String>()
        val instructionType= ArrayList<Int>()
        val instructionDistance= ArrayList<Int>()
        if(route != null) {
            for (instruction in route!!.getRouteInstructions() ) {
                instructionStrings.add(instruction.getHumanTurnInstruction())
                instructionType.add(instruction.turnInstruction)
                instructionDistance.add(instruction.distance)
            }
            listView.setAdapter(
                    DirectionListAdapter(listView.getContext(),
                            instructionStrings,
                            instructionType, instructionDistance, false))
        }
        listView.setOnItemClickListener { adapterView, view, i, l ->
            collapseSlideLayout()
            pager?.setCurrentItem(i - 1)
        }
        findViewById(R.id.route_reverse).setVisibility(View.GONE)
        slideLayout?.setDragView(view.findViewById(R.id.instruction_route_header))
        setHeaderOrigins()
    }

    private fun setHeaderOrigins() {
        (findViewById(R.id.starting_point) as TextView).setText(R.string.current_location)
        (findViewById(R.id.destination) as TextView).setText((findViewById(R.id.destination_name) as TextView).getText())
        findViewById(R.id.starting_location_icon).setVisibility(View.VISIBLE)
        findViewById(R.id.destination_location_icon).setVisibility(View.GONE)
    }

    public fun collapseSlideLayout() {
        if (slideLayoutIsExpanded()) {
            slideLayout?.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        }
    }

    public fun slideLayoutIsExpanded() : Boolean {
        return slideLayout?.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;
    }

    public fun slideLayoutIsCollapsed() : Boolean {
        return slideLayout?.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED;
    }

    private fun turnAutoPageOff() : Boolean {
        if (autoPage) {
            currentInstructionIndex = pager?.getCurrentItem() as Int
        }
        autoPage = false
        return false
    }

    private fun resumeAutoPaging() {
        pager?.setCurrentItem(currentInstructionIndex)
        setCurrentPagerItemStyling(currentInstructionIndex)
        autoPage = true
    }

    private fun setCurrentPagerItemStyling(position : Int) {
        var lastItemIndex = (pager?.getAdapter() as InstructionAdapter).getCount() - 1
        var itemsUntilLastInstruction = (lastItemIndex - position)
        if(itemsUntilLastInstruction ==  1) {
            (pager?.getAdapter() as InstructionAdapter)
                    .setBackgroundColorArrived(findViewByIndex(position + 1))
        }
        if(autoPage) {
            (pager?.getAdapter() as InstructionAdapter)
                    .setBackgroundColorActive(findViewByIndex(position))
        } else {
            if(position == lastItemIndex) {
                (pager?.getAdapter() as InstructionAdapter)
                        .setBackgroundColorArrived(findViewByIndex(position))
            } else {
                (pager?.getAdapter() as InstructionAdapter)
                        .setBackgroundColorInactive(findViewByIndex(position))
            }
        }
    }

    public fun findViewByIndex(index: Int): View? {
        return pager?.findViewWithTag(VIEW_TAG + index)
    }

    /**
     * Route engine callback object
     */
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
            setDistanceToNextInstruction(distanceToNextInstruction)
            setDistanceToDestination(distanceToDestination)
        }

        override fun onInstructionComplete(index: Int) {
            log("[onInstructionComplete]", index)
            currentInstructionIndex += 1
            pager?.setCurrentItem(currentInstructionIndex)
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

    private fun setDistanceToDestination(distanceToDestination: Int) {
        val distanceToDestinationView = findViewById(R.id.destination_distance)
        if (distanceToDestinationView is DistanceView) {
            distanceToDestinationView.distanceInMeters = distanceToDestination
        }
    }

    private fun setDistanceToNextInstruction(distanceToNextInstruction: Int) {
        val currentInstructionView = findViewByIndex(currentInstructionIndex)
        val distanceToNextView = currentInstructionView?.findViewById(R.id.distance)
        if (distanceToNextView is DistanceView) {
            distanceToNextView.distanceInMeters = distanceToNextInstruction
        }
    }

    override fun onLocationChanged(location: Location) {
        if (route != null) {
            routeEngine?.onLocationChanged(location)
        }
    }
}
