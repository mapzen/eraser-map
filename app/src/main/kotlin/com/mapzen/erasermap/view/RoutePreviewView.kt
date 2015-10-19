package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.bindView
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature
import com.mapzen.valhalla.Route

public class RoutePreviewView : RelativeLayout {
    val startView: TextView by bindView(R.id.starting_point)
    val destinationView: TextView by bindView(R.id.destination)

    public var reverse : Boolean = false

    public var destination: SimpleFeature? = null
        set (destination) {
            if(reverse) {
                startView.text = destination?.getTitle()
            } else {
                destinationView.text = destination?.getTitle()
            }
        }

    public var route: Route? = null
        set (route) {
            if(reverse) {
                destinationView.setText(R.string.current_location)
            } else {
                startView.setText(R.string.current_location)
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
                .inflate(R.layout.view_route_preview, this, true)
    }
}
