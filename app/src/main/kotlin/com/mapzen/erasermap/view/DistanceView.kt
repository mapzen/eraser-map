package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.helpers.DistanceFormatter
import javax.inject.Inject

public class DistanceView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {
    init {
        (context.getApplicationContext() as EraserMapApplication).component().inject(this)
    }

    public var distanceInMeters: Int = 0
        set (value) {
            $distanceInMeters = value
            setText(DistanceFormatter.format(value, true, settings?.distanceUnits))
        }

    public var settings: AppSettings? = null
        @Inject set
}
