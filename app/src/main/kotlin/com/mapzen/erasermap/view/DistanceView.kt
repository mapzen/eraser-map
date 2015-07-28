package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.mapzen.helpers.DistanceFormatter

public class DistanceView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {
    public var  distanceInMeters: Int = 0
        set (value) {
            $distanceInMeters = value
            setText(DistanceFormatter.format(value, true))
        }
}
