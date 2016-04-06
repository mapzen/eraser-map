package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature
import com.mapzen.valhalla.Route

class RoutePreviewView : RelativeLayout {
    var startView: TextView? = null
    var destinationView: TextView? = null
    var distancePreview: DistanceView? = null
    var timePreview: TimeView? = null
    var viewListButton: Button? = null
    var startNavigationButton: Button? = null

    var reverse : Boolean = false
        set (value) {
            field = value
            if (value) {
                startView?.text = destination?.name()
                destinationView?.setText(R.string.current_location)
                startNavigationButton?.visibility = GONE
            } else {
                startView?.setText(R.string.current_location)
                destinationView?.text = destination?.name()
                startNavigationButton?.visibility = VISIBLE

            }
        }

    var destination: SimpleFeature? = null
        set (value) {
            field = value
            startView?.setText(R.string.current_location)
            destinationView?.text = value?.name()
        }

    var route: Route? = null
        set (value) {
            field = value
            val distance = value?.getTotalDistance() ?: 0
            distancePreview?.distanceInMeters = distance

            val time = value?.getTotalTime() ?: 0
            timePreview?.timeInMinutes = time / 60
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

        startView = findViewById(R.id.starting_point) as TextView?
        destinationView = findViewById(R.id.destination) as TextView?
        distancePreview = findViewById(R.id.distance_preview) as DistanceView?
        timePreview = findViewById(R.id.time_preview) as TimeView?
        viewListButton = findViewById(R.id.view_list) as Button?
        startNavigationButton = findViewById(R.id.start_navigation) as Button?
    }

    fun disableStartNavigation() {
        startNavigationButton?.isEnabled = false
        viewListButton?.isEnabled = false
    }

    fun enableStartNavigation() {
        startNavigationButton?.isEnabled = true
        viewListButton?.isEnabled = true
    }

}
