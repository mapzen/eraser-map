package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import com.mapzen.erasermap.R

/**
 * Formats time for display. Examples: <1 min, 45 mins, 2 hrs 1 min, 5 hrs 27 mins.
 */
public class TimeView(context: Context, attrs: AttributeSet) : TextView(context, attrs) {
    public var timeInMinutes: Int = 0
        set (value) {
            if (noTime) {
                text = ""
            } else if (value < 1) {
                text = context.getString(R.string.less_than_one_minute)
            } else {
                formatTime(value)
            }
        }

    public var noTime: Boolean = false
        set (value) {
            field = value
        }

    private fun formatTime(value: Int) {
        val hours = value / 60
        val minutes = value % 60

        val hourText = context.resources.getQuantityString(R.plurals.hours, hours, hours)
        val minuteText = context.resources.getQuantityString(R.plurals.minutes, minutes, minutes)

        if (hours == 0) {
            text = minuteText
        } else if (minutes == 0) {
            text = hourText
        } else {
            text = hourText + " " + minuteText
        }
    }
}
