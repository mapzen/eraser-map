package com.mapzen.erasermap.view

import android.content.Context
import android.graphics.Point
import android.location.Location
import android.os.Handler
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
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
import java.util.ArrayList
import javax.inject.Inject

class RouteModeView : LinearLayout, RouteViewController, ViewPager.OnPageChangeListener, DirectionItemClickListener {
    companion object {
        const val VIEW_TAG: String = "Instruction_"
        const val MAP_DATA_NAME_ROUTE_ICON = "mz_route_location"
        const val MAP_DATA_NAME_ROUTE_LINE = "mz_route_line"
        const val MAP_DATA_PROP_TYPE = "type"
        const val MAP_DATA_PROP_POINT = "point"
        const val MAP_DATA_PROP_LINE = "line"
    }

    val mapListToggle: MapListToggleButton by lazy {
        findViewById(R.id.map_list_toggle)as MapListToggleButton
    }
    val routeCancelButton: ImageView by lazy {
        findViewById(R.id.route_cancel) as ImageView
    }
    val directionListView: DirectionListView by lazy {
        findViewById(R.id.direction_list_vew) as DirectionListView
    }
    val distanceToDestination: DistanceView by lazy {
        findViewById(R.id.destination_distance) as DistanceView
    }
    val instructionPager: ViewPager by lazy {
        findViewById(R.id.instruction_pager) as ViewPager
    }
    val resumeButton: Button by lazy {
        findViewById(R.id.resume) as Button
    }
    val destinationNameTextView: TextView by lazy {
        findViewById(R.id.destination_name) as TextView
    }
    val footerSeparator: View by lazy {
        findViewById(R.id.footer_separator)
    }

    var mapController: MapController? = null
        set(value) {
            value?.setPanResponder(object: TouchInput.PanResponder {
                override fun onPan(startX: Float, startY: Float, endX: Float, endY: Float):
                        Boolean {
                    return onMapPan(endX - startX, endY - startY)
                }
                override fun onFling(posX: Float, posY: Float, velocityX: Float, velocityY: Float):
                        Boolean {
                    return false
                }
            })

            field = value
        }

    var autoPage: Boolean = true
    var route: Route? = null
    var mainPresenter: MainPresenter? = null
    var voiceNavigationController: VoiceNavigationController? = null
    var notificationCreator: NotificationCreator? = null
    @Inject lateinit var routePresenter: RoutePresenter
    @Inject lateinit var settings: AppSettings

    private var routeIcon: MapData? = null
    private var routeLine: MapData? = null
    private var previousScrollState: Int = ViewPager.SCROLL_STATE_IDLE
    private var userScrollChange: Boolean = false

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        (context.applicationContext as EraserMapApplication).component().inject(this@RouteModeView)
        (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.view_route_mode, this, true)
        routePresenter.routeController = this
        resumeButton.setOnClickListener {
            onResumeButtonClicked()
        }

        mapListToggle.setOnClickListener {
            routePresenter.onMapListToggleClick(mapListToggle.state)
        }
        routeCancelButton.visibility = View.VISIBLE
        routeCancelButton.setOnClickListener { routePresenter.onRouteCancelButtonClick() }
        directionListView.directionItemClickListener = this
    }

    private fun onResumeButtonClicked() {
        routePresenter.onResumeButtonClick()
        mainPresenter?.updateLocation()
        instructionPager.currentItem = routePresenter.currentInstructionIndex
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        if (instructionPager.currentItem == routePresenter.currentInstructionIndex) {
            setCurrentPagerItemStyling(routePresenter.currentInstructionIndex);
            if (!autoPage) {
                resumeAutoPaging()
            }
        } else {
            setCurrentPagerItemStyling(position)
            autoPage = false
        }
    }

    override fun onPageSelected(position: Int) {
        setCurrentPagerItemStyling(routePresenter.currentInstructionIndex);
        val instruction = route?.getRouteInstructions()?.get(position)
        if (instruction is Instruction) {
            if (userScrollChange) {
                routePresenter.onInstructionSelected(instruction)
                if (position == 0) {
                    onResumeButtonClicked()
                }
            }
        }
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

    fun setAdapter(adapter: PagerAdapter) {
        instructionPager.adapter = adapter
        instructionPager.addOnPageChangeListener(this)
        distanceToDestination.distanceInMeters = route?.getRemainingDistanceToDestination() as Int
        instructionPager.setOnTouchListener({ view, motionEvent -> onPagerTouch() })
    }

    fun setAdapterRerouting(adapter: PagerAdapter) {
        instructionPager.adapter = adapter
        instructionPager.addOnPageChangeListener(null)
        instructionPager.setOnTouchListener(null)
    }

    private fun onPagerTouch(): Boolean {
        routePresenter.onInstructionPagerTouch()
        return false
    }

    private fun resumeAutoPaging() {
        instructionPager.currentItem = routePresenter.currentInstructionIndex
        setCurrentPagerItemStyling(routePresenter.currentInstructionIndex)
        autoPage = true
    }

    private fun setCurrentPagerItemStyling(position : Int) {
        if (instructionPager.adapter is ReroutingAdapter) {
            return
        }
        var lastItemIndex = (instructionPager.adapter as InstructionAdapter).count - 1
        var itemsUntilLastInstruction = (lastItemIndex - position)
        if (itemsUntilLastInstruction == 1) {
            (instructionPager.adapter as InstructionAdapter)
                    .setBackgroundColorArrived(findViewByIndex(position + 1))
        }

        val adapter = instructionPager.adapter
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

    fun findViewByIndex(index: Int): View? {
        return instructionPager.findViewWithTag(VIEW_TAG + index)
    }

    override fun onLocationChanged(location: Location) {
        if (route != null) {
            routePresenter.onLocationChanged(location)
        }
    }

    private fun onMapPan(deltaX: Float, deltaY: Float): Boolean {
        routePresenter.onMapPan(deltaX, deltaY)
        mainPresenter?.onMapMotionEvent()
        return false
    }

    override fun showResumeButton() {
        resumeButton.visibility = View.VISIBLE
    }

    override fun hideResumeButton() {
        resumeButton.visibility = View.GONE
    }

    override fun isResumeButtonHidden(): Boolean {
        return resumeButton.visibility == View.GONE
    }

    override fun showRouteIcon(location: Location) {
        if (routeIcon == null) {
            routeIcon = MapData(MAP_DATA_NAME_ROUTE_ICON)
            Tangram.addDataSource(routeIcon);
        }

        val properties = com.mapzen.tangram.Properties()
        properties.set(MAP_DATA_PROP_TYPE, MAP_DATA_PROP_POINT);

        routeIcon?.clearData()
        routeIcon?.addPoint(properties, LngLat(location.longitude, location.latitude))
        mapController?.requestRender()
    }

    override fun centerMapOnCurrentLocation() {
        val location = routePresenter.currentSnapLocation
        if (location is Location) {
            centerMapOnLocation(location)
        }
    }

    override fun centerMapOnLocation(location: Location) {
        routePresenter.currentSnapLocation = location
        // If the user isnt making the resume button show by scrolling through
        // instruction pager, then they are viewing map at custom view/position
        // and we shouldnt do any centering
        if (!isResumeButtonHidden() && userScrollChange == false) {
            return
        }
        mapController?.queueEvent {

            // Record the initial view configuration
            val lastPosition = mapController?.mapPosition
            val lastRotation = mapController?.mapRotation

            // Update position, rotation, tilt for new location
            mapController?.mapPosition = LngLat(location.longitude, location.latitude)
            mapController?.mapRotation = getBearingInRadians(location)
            mapController?.mapTilt = MainPresenter.ROUTING_TILT
            mapController?.mapZoom = routePresenter.mapZoomLevelForCenterMapOnLocation(location)

            // Get the width and height of the window
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val display = windowManager.defaultDisplay
            val point = Point()
            display.getSize(point)
            val screenWidth = point.x.toDouble()
            val screenHeight = point.y.toDouble()

            // Find the view that will place the current location marker in the lower quarter
            // of the window.
            val nextPosition = mapController?.coordinatesAtScreenPosition(screenWidth/2,
                    screenHeight/3.5) ?: LngLat()
            val nextRotation = getBearingInRadians(location)

            // Return to our initial view to prepare for easing to the next view
            mapController?.mapPosition = lastPosition
            mapController?.mapRotation = lastRotation ?: 0f

            // Begin easing to the next view
            mapController?.setMapPosition(nextPosition.longitude, nextPosition.latitude, 1f,
                    MapController.EaseType.LINEAR)
            mapController?.setMapRotation(nextRotation, 1f, MapController.EaseType.LINEAR)
        }
    }

    override fun updateMapZoom(zoom: Float) {
        mapController?.queueEvent {
            mapController?.setMapZoom(zoom, 1f)
        }
    }

    override fun updateSnapLocation(location: Location) {
        routePresenter.onUpdateSnapLocation(location)
    }

    override fun setCurrentInstruction(index: Int) {
        routePresenter.onSetCurrentInstruction(index)
    }

    override fun displayInstruction(index: Int) {
        routePresenter.currentInstructionIndex = index
        instructionPager.currentItem = index
        directionListView.setCurrent(index)
        notificationCreator?.createNewNotification(
                destinationNameTextView.text.toString(),
                route?.getRouteInstructions()?.get(index)?.getHumanTurnInstruction().toString())
    }

    override fun setMilestone(index: Int, milestone: RouteEngine.Milestone) {
        val instruction = route?.getRouteInstructions()?.get(index)
        val units = settings.distanceUnits
        if (instruction is Instruction && units is Router.DistanceUnits) {
            voiceNavigationController?.playMilestone(instruction, milestone, units)
            val adapter = instructionPager.adapter
            if (adapter is InstructionAdapter) {
                adapter.setBeginText(findViewByIndex(index), instruction)
            }
        }
    }

    override fun playStartInstructionAlert() {
        val instruction = route?.getRouteInstructions()?.get(0)
        if (instruction is Instruction) {
            voiceNavigationController?.playStart(instruction)
        }
    }

    override fun playPreInstructionAlert(index: Int) {
        val instruction = route?.getRouteInstructions()?.get(index)
        val finalInstructionIndex = route?.getRouteInstructions()?.size?.minus(1)
        if (instruction is Instruction && index != finalInstructionIndex) {
            voiceNavigationController?.playPre(instruction)
        }
    }

    override fun playPostInstructionAlert(index: Int) {
        val icon = findViewByIndex(index)?.findViewById(R.id.icon)
        if (icon is ImageView) {
            icon.setImageResource(DisplayHelper.getRouteDrawable(context, 8))
        }

        val instruction = route?.getRouteInstructions()?.get(index)
        if (instruction is Instruction) {
            voiceNavigationController?.playPost(instruction)
            val adapter = instructionPager.adapter
            if (adapter is InstructionAdapter) {
                adapter.setPostText(findViewByIndex(index), instruction)
            }
        }
    }

    override fun updateDistanceToNextInstruction(meters: Int) {
        val currentInstructionView = findViewByIndex(routePresenter.currentInstructionIndex)
        val distanceToNextView = currentInstructionView?.findViewById(R.id.distance)
        if (distanceToNextView is DistanceView) {
            distanceToNextView.distanceInMeters = meters
        }
    }

    override fun updateDistanceToDestination(meters: Int) {
        distanceToDestination.distanceInMeters = meters
    }

    override fun setRouteComplete() {
        resumeButton.visibility = View.GONE
        displayInstruction(instructionPager.adapter?.count?.minus(1) ?: 0)
        notificationCreator?.killNotification()
        distanceToDestination.distanceInMeters = 0
        footerSeparator.visibility = View.GONE
        playFinalVerbalInstruction()

        val location = route?.getGeometry()?.get(route?.getGeometry()?.size?.minus(1) ?: 0)
        if (location is Location) {
            centerMapOnLocation(location)
            showRouteIcon(location)
        }
    }

    private fun playFinalVerbalInstruction() {
        val finalInstructionIndex = route?.getRouteInstructions()?.size?.minus(1) ?: 0
        val finalInstruction = route?.getRouteInstructions()?.get(finalInstructionIndex)
        if (finalInstruction is Instruction) voiceNavigationController?.playPre(finalInstruction)
    }

    override fun showReroute(location: Location) {
        setAdapterRerouting(ReroutingAdapter(context))
        mainPresenter?.onReroute(location)
    }

    private fun getBearingInRadians(location: Location): Float {
        return Math.toRadians(360 - location.bearing.toDouble()).toFloat()
    }

    override fun hideRouteIcon() {
        routeIcon?.clear()
    }

    fun startRoute(destination: Feature, route: Route?) {
        this.route = route
        initStartLocation()
        initDestination(destination)
        initInstructionAdapter()
        this.visibility = View.VISIBLE
        routePresenter.onRouteStart(route)
        footerSeparator.visibility = View.VISIBLE
    }

    fun resumeRoute(destination: Feature, route: Route?) {
        this.route = route
        initDestination(destination)
        initInstructionAdapter()
        this.visibility = View.VISIBLE
        routePresenter.onRouteResume(route)
        Handler().postDelayed( {
            val currentSnapLocation = routePresenter.currentSnapLocation
            if (currentSnapLocation != null) {
                showRouteIcon(currentSnapLocation)
            }
            centerMapOnCurrentLocation()
        }, 100)
    }

    private fun initStartLocation() {
        val startingLocation = route?.getRouteInstructions()?.get(0)?.location
        if (startingLocation is Location) {
            centerMapOnLocation(startingLocation)
            showRouteIcon(startingLocation)
        }
    }

    private fun initDestination(destination: Feature) {
        val simpleFeature = SimpleFeature.fromFeature(destination)
        destinationNameTextView.text = simpleFeature.name()
    }

    private fun initInstructionAdapter() {
        val instructions = route?.getRouteInstructions()
        if (instructions != null) {
            val adapter = InstructionAdapter(context, instructions)
            setAdapter(adapter)
        }
    }

    fun drawRoute(route: Route) {
        val properties = com.mapzen.tangram.Properties()
        properties.set(MAP_DATA_PROP_TYPE, MAP_DATA_PROP_LINE);
        val geometry: ArrayList<Location>? = route.getGeometry()
        val mapGeometry: ArrayList<LngLat> = ArrayList()
        if (geometry is ArrayList<Location>) {
            for (location in geometry) {
                mapGeometry.add(LngLat(location.longitude, location.latitude))
            }
        }

        if (routeLine == null) {
            routeLine = MapData(MAP_DATA_NAME_ROUTE_LINE)
            Tangram.addDataSource(routeLine);
        }

        routeLine?.clear()
        routeLine?.addLine(properties, mapGeometry)
        mapController?.requestRender()
    }

    override fun hideRouteLine() {
        routeLine?.clear()
    }

    fun clearRoute() {
        routePresenter.onRouteClear()
    }

    override fun showRouteDirectionList() {
        val instructions = route?.getRouteInstructions()
        if (instructions != null && instructions.size > 0) {
            directionListView.setInstructions(instructions)
            directionListView.setCurrent(instructionPager.currentItem)
            directionListView.visibility = View.VISIBLE
        }
        if (resumeButton.visibility == View.VISIBLE) {
            resumeButton.visibility = View.INVISIBLE
        }

        mapListToggle.state = MapListToggleButton.MapListState.MAP
        routeCancelButton.visibility = View.GONE
    }

    override fun hideRouteDirectionList() {
        if (resumeButton.visibility == View.INVISIBLE) {
            resumeButton.visibility = View.VISIBLE
        }
        directionListView.visibility = View.GONE
        mapListToggle.state = MapListToggleButton.MapListState.LIST
        routeCancelButton.visibility = View.VISIBLE
    }

    override fun onDirectionItemClicked(position: Int) {
        hideRouteDirectionList()
        routePresenter.onInstructionPagerTouch()
        userScrollChange = true
        instructionPager.setCurrentItem(position, true)
        directionListView.setCurrent(position)
    }
}
