package com.mapzen.erasermap.view

import android.content.Context
import android.graphics.Point
import android.location.Location
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.erasermap.presenter.RoutePresenter
import com.mapzen.erasermap.util.DisplayHelper
import com.mapzen.erasermap.util.NotificationCreator
import com.mapzen.helpers.RouteEngine
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.MapController
import com.mapzen.tangram.MapData
import com.mapzen.tangram.Tangram
import com.mapzen.tangram.TouchInput
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import java.util.ArrayList
import javax.inject.Inject

public class RouteModeView : LinearLayout, RouteViewController, ViewPager.OnPageChangeListener {
    companion object {
        val VIEW_TAG: String = "Instruction_"
    }

    public val SLIDING_PANEL_OFFSET_OPEN: Float = 0.1f

    var mapController: MapController? = null
        set(value) {
            value?.setPanResponder(object: TouchInput.PanResponder {
                override fun onPan(startX: Float, startY: Float, endX: Float, endY: Float): Boolean {
                    return onMapPan(endX - startX, endY - startY)
                }
                override fun onFling(posX: Float, posY: Float, velocityX: Float, velocityY: Float): Boolean {
                    return false
                }
            })

            field = value
        }

    var pager: ViewPager? = null
    var autoPage: Boolean = true
    var route: Route? = null
    var slideLayout: SlidingUpPanelLayout? = null
    var panelListener: SlidingUpPanelLayout.PanelSlideListener? = null
    var mainPresenter: MainPresenter? = null
    var voiceNavigationController: VoiceNavigationController? = null
    var notificationCreator: NotificationCreator? = null
    var routePresenter: RoutePresenter? = null
        @Inject set
    var settings: AppSettings? = null
        @Inject set

    private var currentSnapLocation: Location? = null
    private var routeIcon: MapData? = null
    private var routeLine: MapData? = null
    private var previousScrollState: Int = ViewPager.SCROLL_STATE_IDLE
    private var userScrollChange: Boolean = false

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
            pager?.currentItem = routePresenter?.currentInstructionIndex
        }
        initSlideLayout(findViewById(R.id.sliding_layout))
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (pager?.currentItem == routePresenter?.currentInstructionIndex) {
            setCurrentPagerItemStyling(routePresenter?.currentInstructionIndex ?: 0);
            if (!autoPage) {
                resumeAutoPaging()
            }
        } else {
            setCurrentPagerItemStyling(position)
            autoPage = false
        }
    }

    override fun onPageSelected(position: Int) {
        setCurrentPagerItemStyling(routePresenter?.currentInstructionIndex ?: 0);
        val instruction = route?.getRouteInstructions()?.get(position)
        if (instruction is Instruction) {
            if (userScrollChange) {
                routePresenter?.onInstructionSelected(instruction)
            }
        }
        notificationCreator?.createNewNotification((findViewById(R.id.destination_name) as TextView).text.toString(),
                route?.getCurrentInstruction()?.getHumanTurnInstruction().toString())
    }

    override fun onPageScrollStateChanged(state: Int) {
        if (previousScrollState == ViewPager.SCROLL_STATE_DRAGGING
                && state == ViewPager.SCROLL_STATE_SETTLING) {
            userScrollChange = true
        } else if (previousScrollState == ViewPager.SCROLL_STATE_SETTLING
                && state == ViewPager.SCROLL_STATE_IDLE) {
            userScrollChange = false
        }

        previousScrollState = state
    }

    public fun setAdapter(adapter: PagerAdapter) {
        pager = findViewById(R.id.instruction_pager) as ViewPager
        pager?.adapter = adapter
        pager?.addOnPageChangeListener(this)
        (findViewById(R.id.destination_distance) as DistanceView).distanceInMeters =
                (route?.getRemainingDistanceToDestination() as Int)
        pager?.setOnTouchListener({ view, motionEvent -> onPagerTouch() })
        notificationCreator?.createNewNotification((findViewById(R.id.destination_name) as TextView).text.toString(),
                route?.getCurrentInstruction()?.getHumanTurnInstruction().toString())
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
        pager?.currentItem = routePresenter?.currentInstructionIndex
        setCurrentPagerItemStyling(routePresenter?.currentInstructionIndex ?:0)
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

    private fun onMapPan(deltaX: Float, deltaY: Float): Boolean {
        routePresenter?.onMapPan(deltaX, deltaY)
        mainPresenter?.onMapMotionEvent()
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
            Tangram.addDataSource(routeIcon);
        }

        val properties = com.mapzen.tangram.Properties()
        properties.add("type", "point");

        routeIcon?.clearData()
        routeIcon?.addPoint(properties, LngLat(location.longitude, location.latitude))
        mapController?.requestRender()

    }

    override fun centerMapOnCurrentLocation() {
        val location = currentSnapLocation
        if (location is Location) {
            centerMapOnLocation(location)
        }
    }

    override fun centerMapOnLocation(location: Location) {
        currentSnapLocation = location
        mapController?.queueEvent {

            // Record the initial view configuration
            val lastPosition = mapController?.mapPosition
            val lastRotation = mapController?.mapRotation

            // Update position, rotation, tilt, and zoom for new location
            mapController?.mapPosition = LngLat(location.longitude, location.latitude)
            mapController?.mapRotation = getBearingInRadians(location)
            mapController?.mapZoom = MainPresenter.ROUTING_ZOOM
            mapController?.mapTilt = MainPresenter.ROUTING_TILT

            // Get the width and height of the window
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val point = Point()
            display.getSize(point)
            val screenWidth = point.x.toDouble()
            val screenHeight = point.y.toDouble()

            // Find the view that will place the current location marker in the lower quarter of the window
            val nextPosition = mapController?.coordinatesAtScreenPosition(screenWidth/2, screenHeight/4) ?: LngLat()
            val nextRotation = getBearingInRadians(location)

            // Return to our initial view to prepare for easing to the next view
            mapController?.mapPosition = lastPosition
            mapController?.mapRotation = lastRotation

            // Begin easing to the next view
            mapController?.setMapPosition(nextPosition.longitude, nextPosition.latitude, 1f, MapController.EaseType.LINEAR)
            mapController?.setMapRotation(nextRotation, 1f, MapController.EaseType.LINEAR)
        }
    }

    override fun updateSnapLocation(location: Location) {
        routePresenter?.onUpdateSnapLocation(location)
    }

    override fun setCurrentInstruction(index: Int) {
        routePresenter?.currentInstructionIndex = index
        pager?.currentItem = index
    }

    override fun setMilestone(index: Int, milestone: RouteEngine.Milestone) {
        val instruction = route?.getRouteInstructions()?.get(index)
        val units = settings?.distanceUnits
        if (instruction is Instruction && units is Router.DistanceUnits) {
            voiceNavigationController?.playMilestone(instruction, milestone, units)
        }
    }

    override fun playStartInstructionAlert() {
        val instruction = route?.getRouteInstructions()?.get(0)
        if (instruction is Instruction) voiceNavigationController?.playStart(instruction)
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
        }
    }

    override fun updateDistanceToNextInstruction(meters: Int) {
        val currentInstructionView = findViewByIndex(routePresenter?.currentInstructionIndex ?: 0)
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
        voiceNavigationController?.playRecalculating()
        mainPresenter?.onReroute(location)
    }

    private fun getBearingInRadians(location: Location): Float {
        return Math.toRadians(360 - location.bearing.toDouble()).toFloat()
    }

    override fun hideRouteIcon() {
        routeIcon?.clear()
    }

    public fun startRoute(destination: Feature, route: Route?) {
        this.route = route
        initStartLocation()
        initDestination(destination)
        initInstructionAdapter()
        this.visibility = View.VISIBLE
        routePresenter?.onRouteStart(route)
    }

    public fun resumeRoute(destination: Feature, route: Route?) {
        this.route = route
        initDestination(destination)
        initInstructionAdapter()
        this.visibility = View.VISIBLE
        routePresenter?.onRouteResume(route)
    }

    private fun initStartLocation() {
        val startingLocation = route?.getRouteInstructions()?.get(0)?.location
        if (startingLocation is Location) {
            centerMapOnLocation(startingLocation)
        }
    }

    private fun initDestination(destination: Feature) {
        val simpleFeature = SimpleFeature.fromFeature(destination)
        (findViewById(R.id.destination_name) as TextView).text = simpleFeature.label()
    }

    private fun initInstructionAdapter() {
        val instructions = route?.getRouteInstructions()
        if (instructions != null) {
            val adapter = InstructionAdapter(context, instructions, this)
            setAdapter(adapter)
        }
    }

    public fun drawRoute(route: Route) {
        val properties = com.mapzen.tangram.Properties()
        properties.add("type", "line");
        val geometry: ArrayList<Location>? = route.getGeometry()
        val mapGeometry: ArrayList<LngLat> = ArrayList()
        if (geometry is ArrayList<Location>) {
            for (location in geometry) {
                mapGeometry.add(LngLat(location.longitude, location.latitude))
            }
        }

        if (routeLine == null) {
            routeLine = MapData("route")
            Tangram.addDataSource(routeLine);
        }

        routeLine?.clear()
        routeLine?.addLine(properties, mapGeometry)
        mapController?.requestRender()
    }

    override fun hideRouteLine() {
        routeLine?.clear()
    }

    public fun clearRoute() {
        routePresenter?.onRouteClear()
    }
}
