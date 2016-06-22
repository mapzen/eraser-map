package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.mapzen.android.MapzenDistanceFormatter
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.model.AppSettings
import javax.inject.Inject

public class DistanceView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {

    @Inject lateinit var settings: AppSettings

    var realTime = false

    init {
        (context.applicationContext as EraserMapApplication).component().inject(this)
    }

    public var distanceInMeters: Int = 0
        set (value) {
            field = value
            text = MapzenDistanceFormatter.format(value, realTime, settings.distanceUnits)
        }
}
