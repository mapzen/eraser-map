package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
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

}
