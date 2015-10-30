package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.RelativeLayout.GONE
import android.widget.RelativeLayout.VISIBLE
import android.widget.TextView
import butterknife.bindView
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature
import com.mapzen.valhalla.Route

public class RoutePreviewView : RelativeLayout {
    val startView: TextView by bindView(R.id.starting_point)
    val destinationView: TextView by bindView(R.id.destination)
    val distancePreview: DistanceView by bindView(R.id.distance_preview)
    val timePreview: TimeView by bindView(R.id.time_preview)
    val viewListButton: Button by bindView(R.id.view_list)
    val startNavigationButton: Button by bindView(R.id.start_navigation)

    public var reverse : Boolean = false
        set (value) {
            field = value
            if (value) {
                startView.text = destination?.title
                destinationView.setText(R.string.current_location)
                startNavigationButton.visibility = GONE
            } else {
                startView.setText(R.string.current_location)
                destinationView.text = destination?.title
                startNavigationButton.visibility = VISIBLE

            }
        }

    public var destination: SimpleFeature? = null
        set (value) {
            field = value
            startView.setText(R.string.current_location)
            destinationView.text = value?.title
        }

    public var route: Route? = null
        set (value) {
            field = value
            val distance = value?.getTotalDistance() ?: 0
            distancePreview.distanceInMeters =  distance

            val time = value?.getTotalTime() ?: 0
            timePreview.timeInMinutes = time / 60
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
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.view_route_preview, this, true)
    }
}
