package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature

public class RoutePreviewView : RelativeLayout {
    public var destination: SimpleFeature? = null
    set (destination) {
        (findViewById(R.id.destination) as TextView).setText("Route from current location to "
                + destination?.getTitle())
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
