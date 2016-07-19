package com.mapzen.erasermap.view

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.erasermap.controller.MainActivity
import com.mapzen.erasermap.model.MultiModalHelper
import com.mapzen.erasermap.model.RouteManager
import com.mapzen.pelias.SimpleFeature
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
import java.util.ArrayList
import java.util.concurrent.TimeUnit

class RoutePreviewView : RelativeLayout {
    val startView: TextView by lazy { findViewById(R.id.starting_point) as TextView }
    val destinationView: TextView by lazy { findViewById(R.id.destination) as TextView }
    val distancePreview: DistanceView by lazy { findViewById(R.id.distance_preview) as DistanceView }
    val timePreview: TimeView by lazy { findViewById(R.id.time_preview) as TimeView }
    val viewListButton: Button by lazy { findViewById(R.id.view_list) as Button }
    val startNavigationButton: Button by lazy { findViewById(R.id.start_navigation) as Button }
    val noRouteFound: TextView by lazy { findViewById(R.id.no_route_found) as TextView }
    val tryAnotherMode: TextView by lazy { findViewById(R.id.try_another_mode) as TextView }
    val routeTopContainer: RelativeLayout by lazy { findViewById(R.id.main_content) as RelativeLayout }
    val routeBtmContainer: LinearLayout by lazy { findViewById(R.id.bottom_content) as LinearLayout }
    val previewDirectionListView: DirectionListView by lazy { findViewById(R.id.list_view) as DirectionListView }
    val previewToggleBtn: View by lazy { findViewById(R.id.map_list_toggle) }
    val balancerView: View by lazy { findViewById(R.id.balancer) }
    val distanceView: DistanceView by lazy { findViewById(R.id.destination_distance) as DistanceView }
    val destinationNameTextView: TextView by lazy { findViewById(R.id.destination_name) as TextView }
    var divider: Drawable? = null
    var dividerHeight: Int? = null


    var reverse : Boolean = false
        set (value) {
            field = value
            if (value) {
                startView.text = destination?.name()
                destinationView.setText(R.string.current_location)
                startNavigationButton.visibility = GONE
            } else {
                startView.setText(R.string.current_location)
                destinationView.text = destination?.name()
                startNavigationButton.visibility = VISIBLE

            }
        }

    var destination: SimpleFeature? = null
        set (value) {
            field = value
            startView.setText(R.string.current_location)
            destinationView.text = value?.name()
        }

    var route: Route? = null
        set (value) {
            field = value
            val distance = value?.getTotalDistance() ?: 0
            distancePreview.distanceInMeters = distance

            val time = value?.getTotalTime() ?: 0
            timePreview.timeInMinutes = TimeUnit.SECONDS.toMinutes(time.toLong()).toInt()
        }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int)
    : super(context, attrs, defStyleAttr) {
        init()
    }

    private fun init() {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.view_route_preview, this, true)
    }

    fun disableStartNavigation() {
        startNavigationButton.visibility = View.GONE
        viewListButton.visibility = View.GONE
        noRouteFound.visibility = View.VISIBLE
        tryAnotherMode.visibility = View.VISIBLE
    }

    fun enableStartNavigation(type: Router.Type) {
        if (type != Router.Type.MULTIMODAL) {
            startNavigationButton.visibility = View.VISIBLE
        }
        viewListButton.visibility = View.VISIBLE
        noRouteFound.visibility = View.GONE
        tryAnotherMode.visibility = View.GONE
    }

    fun showDirectionsListView(routeManager: RouteManager, windowManager: WindowManager, compass: View) {
        val instructions = routeManager.route?.getRouteInstructions()
        val simpleFeature = SimpleFeature.fromFeature(routeManager.destination)
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size);
        val height = size.y.toFloat();
        previewToggleBtn.visibility = View.GONE
        balancerView.visibility = View.GONE
        routeBtmContainer.translationY = height
        routeBtmContainer.visibility = View.VISIBLE
        distanceView.distanceInMeters = routeManager.route?.getTotalDistance() as Int
        destinationNameTextView.text = simpleFeature.name()
        if (routeManager.type == Router.Type.MULTIMODAL) {
            val instructionGrouper = InstructionGrouper(instructions as ArrayList<Instruction>)
            previewDirectionListView.adapter = MultiModalDirectionListAdapter(this.context, instructionGrouper,
                routeManager.reverse, MultiModalHelper())
            if (divider == null) {
                divider = previewDirectionListView.divider
                dividerHeight = previewDirectionListView.dividerHeight
            }
            previewDirectionListView.divider = null;
            previewDirectionListView.dividerHeight = 0;
        } else {
            val instructionStrings = ArrayList<String>()
            val instructionTypes = ArrayList<Int>()
            val instructionDistances = ArrayList<Int>()
            if (instructions != null) {
                for(instruction in instructions) {
                    val humanInstruction = instruction.getHumanTurnInstruction()
                    if (humanInstruction is String) {
                        instructionStrings.add(humanInstruction)
                    }
                    instructionTypes.add(instruction.turnInstruction)
                    instructionDistances.add(instruction.distance)
                }
            }
            previewDirectionListView.adapter = DirectionListAdapter(this.context, instructionStrings,
                instructionTypes, instructionDistances, routeManager.reverse)
            if (divider != null) {
                previewDirectionListView.divider = divider
                previewDirectionListView.dividerHeight = dividerHeight as Int
            }
        }

        val topContainerAnimator = ObjectAnimator.ofFloat(routeTopContainer, View.TRANSLATION_Y,-height)
        val btmContainerAnimator = ObjectAnimator.ofFloat(routeBtmContainer, View.TRANSLATION_Y, 0f)
        val animations = AnimatorSet()
        animations.playTogether(topContainerAnimator, btmContainerAnimator)
        animations.duration = MainActivity.DIRECTION_LIST_ANIMATION_DURATION
        animations.interpolator = AccelerateDecelerateInterpolator()
        animations.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                compass.visibility = View.GONE
            }
            override fun onAnimationEnd(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
        })
        animations.start()
    }

    fun hideDirectionsListView(windowManager: WindowManager, compass: View) {
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getSize(size);
        val height = size.y.toFloat();
        val topContainerAnimator = ObjectAnimator.ofFloat(routeTopContainer, View.TRANSLATION_Y, 0f)
        val btmContainerAnimator = ObjectAnimator.ofFloat(routeBtmContainer, View.TRANSLATION_Y, height)
        val animations = AnimatorSet()
        animations.playTogether(topContainerAnimator, btmContainerAnimator)
        animations.duration = MainActivity.DIRECTION_LIST_ANIMATION_DURATION
        animations.interpolator = AccelerateDecelerateInterpolator()
        animations.addListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {
                compass.visibility = View.VISIBLE

            }
            override fun onAnimationEnd(animation: Animator) {
                routeBtmContainer.visibility = View.GONE
            }
            override fun onAnimationRepeat(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
        })
        animations.start()
    }

}
