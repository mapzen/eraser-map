package com.mapzen.erasermap.view

import android.content.Context
import android.location.Location
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.*
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature
import com.mapzen.osrm.Route
import com.mapzen.osrm.Router


public class RoutePreviewView : RelativeLayout {
    public var destination: SimpleFeature? = null
    set (destination) {
        (findViewById(R.id.destination) as TextView).setText(destination?.getTitle())
    }

    public var route: Route? = null
        set (route) {
            (findViewById(R.id.starting_point) as TextView).setText("Current Location")
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
