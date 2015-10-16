package com.mapzen.erasermap.view

import android.content.Context
import android.location.Location
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.*
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.erasermap.presenter.RoutePresenter
import com.mapzen.erasermap.util.DisplayHelper
import com.mapzen.helpers.RouteEngine
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.MapController
import com.mapzen.tangram.MapData
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import java.util.*
import javax.inject.Inject

public class RouteModeView : LinearLayout, RouteViewController, ViewPager.OnPageChangeListener {
    companion object {
        val VIEW_TAG: String = "Instruction_"
    }

    public val SLIDING_PANEL_OFFSET_OPEN: Float = 0.1f

    var mapController: MapController? = null
        set(value) {
            value?.setGenericMotionEventListener(View.OnGenericMotionListener {
                view, event -> onMapMotionEvent()
            })

            $mapController = value
        }

    var pager: ViewPager? = null
    var autoPage: Boolean = true
    var route: Route? = null
    var slideLayout: SlidingUpPanelLayout? = null
    var panelListener: SlidingUpPanelLayout.PanelSlideListener? = null
    var mainPresenter: MainPresenter? = null
    var voiceNavigationController: VoiceNavigationController? = null
    var routePresenter: RoutePresenter? = null
        @Inject set
    var settings: AppSettings? = null
        @Inject set

    private var currentInstructionIndex: Int = 0
    private var currentSnapLocation: Location? = null
    private var routeIcon: MapData? = null

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
        (context.applicationContext as EraserMapApplication).component().inject(this@RouteModeView)
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.view_route_mode, this, true)
        routePresenter?.routeController = this
        (findViewById(R.id.resume) as Button).setOnClickListener {
            routePresenter?.onResumeButtonClick()
            pager?.currentItem = currentInstructionIndex
        }
        initSlideLayout(findViewById(R.id.sliding_layout))
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (pager?.currentItem == currentInstructionIndex) {
            setCurrentPagerItemStyling(currentInstructionIndex);
            if (!autoPage) {
                resumeAutoPaging()
            }
        } else {
            setCurrentPagerItemStyling(position)
            autoPage = false
        }
    }

    override fun onPageSelected(position: Int) {
        setCurrentPagerItemStyling(currentInstructionIndex);
        val instruction = route?.getRouteInstructions()?.get(position)
        if (instruction is Instruction) {
            routePresenter?.onInstructionSelected(instruction)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    public fun setAdapter(adapter: PagerAdapter) {
        pager = findViewById(R.id.instruction_pager) as ViewPager
        pager?.adapter = adapter
        pager?.addOnPageChangeListener(this)
        (findViewById(R.id.destination_distance) as DistanceView).distanceInMeters =
                (route?.getRemainingDistanceToDestination() as Int)
        pager?.setOnTouchListener({ view, motionEvent -> onPagerTouch() })
    }

    private fun onPagerTouch(): Boolean {
        routePresenter?.onInstructionPagerTouch()
        return false
    }

    public fun pageForward(position: Int) {
        pager?.currentItem = position + 1
    }

    public fun pageBackwards(position: Int) {
        pager?.currentItem = position - 1
    }

    public fun initSlideLayout(view: View) {
        slideLayout = view as SlidingUpPanelLayout
        slideLayout?.setDragView(view.findViewById(R.id.drag_area))
        panelListener = getPanelSlideListener(view)
        slideLayout?.setPanelSlideListener(panelListener)
        slideLayout?.isTouchEnabled = false
        findViewById(R.id.drag_area).setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                slideLayout?.isTouchEnabled = true
                return true
            }
        })
        findViewById(R.id.instruction_route_header).setOnTouchListener(object: View.OnTouchListener {
            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                slideLayout?.isTouchEnabled = true
                return true
            }
        })
    }

    public fun getPanelSlideListener(view: View): SlidingUpPanelLayout.PanelSlideListener {
        return (object:SlidingUpPanelLayout.PanelSlideListener {

            public override fun onPanelSlide(panel:View, slideOffset:Float) {
                if (slideOffset >=  SLIDING_PANEL_OFFSET_OPEN) {
                    mainPresenter?.onSlidingPanelOpen()
                }

                if (slideOffset <  SLIDING_PANEL_OFFSET_OPEN) {
                    mainPresenter?.onSlidingPanelCollapse()
                }

                if (slideOffset == SLIDING_PANEL_OFFSET_OPEN) {
                    slideLayout?.isTouchEnabled = false
                }
            }

            public override fun onPanelExpanded(panel:View) { }

            public override fun onPanelCollapsed(panel: View) {
                slideLayout?.isTouchEnabled = false
            }

            public override fun onPanelAnchored(panel:View) { }

            public override fun onPanelHidden(view:View) { }
        })
    }

    override fun showDirectionList()  {
        findViewById(R.id.footer).visibility = View.GONE
        val listView = findViewById(R.id.instruction_list_view) as ListView
        val instructionStrings = ArrayList<String>()
        val instructionType= ArrayList<Int>()
        val instructionDistance= ArrayList<Int>()

        val instructions = route?.getRouteInstructions()
        if (route is Route && instructions is ArrayList<Instruction>) {
            for (instruction in  instructions) {
                val humanInstruction = instruction.getHumanTurnInstruction()
                if (humanInstruction is String) {
                    instructionStrings.add(humanInstruction)
                }
                instructionType.add(instruction.turnInstruction)
                instructionDistance.add(instruction.distance)
            }

            listView.adapter = DirectionListAdapter(listView.context, instructionStrings,
                    instructionType, instructionDistance, false)
        }
        listView.setOnItemClickListener { adapterView, view, i, l ->
            collapseSlideLayout()
            pager?.currentItem = i - 1
        }
        findViewById(R.id.route_reverse).visibility = View.GONE
        slideLayout?.setDragView(slideLayout?.findViewById(R.id.instruction_route_header))
        setHeaderOrigins()
    }

    override fun hideDirectionList() {
        findViewById(R.id.footer).visibility = View.VISIBLE
        slideLayout?.setDragView(slideLayout?.findViewById(R.id.drag_area))
    }

    private fun setHeaderOrigins() {
        (findViewById(R.id.starting_point) as TextView).setText(R.string.current_location)
        (findViewById(R.id.destination) as TextView).text =
                (findViewById(R.id.destination_name) as TextView).text
        findViewById(R.id.starting_location_icon).visibility = View.VISIBLE
        findViewById(R.id.destination_location_icon).visibility = View.GONE
    }

    override fun collapseSlideLayout() {
        if (slideLayoutIsExpanded()) {
            slideLayout?.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED;
        }
    }

    public fun slideLayoutIsExpanded() : Boolean {
        return slideLayout?.panelState == SlidingUpPanelLayout.PanelState.EXPANDED;
    }

    private fun resumeAutoPaging() {
        pager?.currentItem = currentInstructionIndex
        setCurrentPagerItemStyling(currentInstructionIndex)
        autoPage = true
    }

    private fun setCurrentPagerItemStyling(position : Int) {
        var lastItemIndex = (pager?.adapter as InstructionAdapter).count - 1
        var itemsUntilLastInstruction = (lastItemIndex - position)
        if (itemsUntilLastInstruction == 1) {
            (pager?.adapter as InstructionAdapter)
                    .setBackgroundColorArrived(findViewByIndex(position + 1))
        }

        val adapter = pager?.adapter
        if (adapter is InstructionAdapter) {
            if (autoPage) {
                adapter.setBackgroundColorActive(findViewByIndex(position))
            } else {
                if (position == lastItemIndex) {
                    adapter.setBackgroundColorArrived(findViewByIndex(position))
                } else {
                    adapter.setBackgroundColorInactive(findViewByIndex(position))
                }
            }
        }
    }

    public fun findViewByIndex(index: Int): View? {
        return pager?.findViewWithTag(VIEW_TAG + index)
    }

    override fun onLocationChanged(location: Location) {
        if (route != null) {
            routePresenter?.onLocationChanged(location)
        }
    }

    private fun onMapMotionEvent(): Boolean {
        routePresenter?.onMapGesture()
        return false
    }

    override fun showResumeButton() {
        findViewById(R.id.resume).visibility = View.VISIBLE
    }

    override fun hideResumeButton() {
        findViewById(R.id.resume).visibility = View.GONE
    }

    override fun showRouteIcon(location: Location) {
        if (routeIcon == null) {
            routeIcon = MapData("route_icon")
        }

        routeIcon?.clear()
        routeIcon?.addPoint(LngLat(location.longitude, location.latitude))
    }

    override fun centerMapOnCurrentLocation() {
        val location = currentSnapLocation
        if (location is Location) {
            centerMapOnLocation(location)
        }
    }

    override fun centerMapOnLocation(location: Location) {
        currentSnapLocation = location
        mapController?.mapPosition = LngLat(location.longitude, location.latitude)
        mapController?.mapRotation = getBearingInRadians(location)
        mapController?.mapZoom = MainPresenter.ROUTING_ZOOM
        mapController?.mapTilt = MainPresenter.ROUTING_TILT
    }

    override fun updateSnapLocation(location: Location) {
        routePresenter?.onUpdateSnapLocation(location)
    }

    override fun setCurrentInstruction(index: Int) {
        currentInstructionIndex = index
        pager?.currentItem = index
    }

    override fun setMilestone(index: Int, milestone: RouteEngine.Milestone) {
        val instruction = route?.getRouteInstructions()?.get(index)
        val units = settings?.distanceUnits
        if (instruction is Instruction && units is Router.DistanceUnits) {
            voiceNavigationController?.playMilestone(instruction, milestone, units)
        }
    }

    override fun playPreInstructionAlert(index: Int) {
        val instruction = route?.getRouteInstructions()?.get(index)
        if (instruction is Instruction) voiceNavigationController?.playPre(instruction)
    }

    override fun playPostInstructionAlert(index: Int) {
        val icon = findViewByIndex(index)?.findViewById(R.id.icon)
        if (icon is ImageView) {
            icon.setImageResource(DisplayHelper.getRouteDrawable(getContext(), 8))
        }

        val instruction = route?.getRouteInstructions()?.get(index)
        if (instruction is Instruction) {
            voiceNavigationController?.playPost(instruction)
            routePresenter?.onInstructionSelected(instruction)
        }
    }

    override fun updateDistanceToNextInstruction(meters: Int) {
        val currentInstructionView = findViewByIndex(currentInstructionIndex)
        val distanceToNextView = currentInstructionView?.findViewById(R.id.distance)
        if (distanceToNextView is DistanceView) {
            distanceToNextView.distanceInMeters = meters
        }
    }

    override fun updateDistanceToDestination(meters: Int) {
        val distanceToDestinationView = findViewById(R.id.destination_distance)
        if (distanceToDestinationView is DistanceView) {
            distanceToDestinationView.distanceInMeters = meters
        }
    }

    override fun showRouteComplete() {
        findViewById(R.id.footer_wrapper)?.visibility = View.GONE
        findViewById(R.id.resume)?.visibility = View.GONE
        findViewById(R.id.instruction_list)?.visibility = View.GONE
        (findViewById(R.id.sliding_layout) as SlidingUpPanelLayout).shadowHeight = 0
    }

    override fun showReroute(location: Location) {
        mainPresenter?.onReroute(location)
    }

    private fun getBearingInRadians(location: Location): Float {
        return Math.toRadians(360 - location.bearing.toDouble()).toFloat()
    }

    fun hideRouteIcon() {
        routeIcon?.clear()
    }

    public fun startRoute(destination: Feature, route: Route?) {
        this.route = route
        routePresenter?.onRouteStart(route)
        initStartLocation()
        initDestination(destination)
        initInstructionAdapter()
        this.visibility = View.VISIBLE
    }

    private fun initStartLocation() {
        val startingLocation = route?.getRouteInstructions()?.get(0)?.location
        if (startingLocation is Location) {
            centerMapOnLocation(startingLocation)
        }
    }

    private fun initDestination(destination: Feature) {
        val simpleFeature = SimpleFeature.fromFeature(destination)
        (findViewById(R.id.destination_name) as TextView).text = simpleFeature.toString()
    }

    private fun initInstructionAdapter() {
        val instructions = route?.getRouteInstructions()
        if (instructions != null) {
            val adapter = InstructionAdapter(context, instructions, this)
            setAdapter(adapter)
        }
    }
}
