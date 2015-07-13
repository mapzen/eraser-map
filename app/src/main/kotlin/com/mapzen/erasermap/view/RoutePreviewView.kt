package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature
import com.mapzen.valhalla.Route


public class RoutePreviewView : RelativeLayout {
    public var reverse : Boolean = false
    public var destination: SimpleFeature? = null
        set (destination) {
            if(reverse) {
                (findViewById(R.id.starting_point) as TextView).setText(destination?.getTitle())
            } else {
                (findViewById(R.id.destination) as TextView).setText(destination?.getTitle())
            }
        }

    public var route: Route? = null
        set (route) {
            if(reverse) {
                (findViewById(R.id.destination) as TextView).setText(R.string.current_location)
            } else {
                (findViewById(R.id.starting_point) as TextView).setText(R.string.current_location)
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
