package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import com.mapzen.erasermap.R

public class CompassView(context: Context, attrs: AttributeSet) : ButtonView(context, attrs) {

    override fun idForLayout():Int {
        return R.layout.view_compass;
    }

    override fun idForImage():Int {
        return R.id.compass;
    }

    override fun setRotation(rotation: Float) {
        image.rotation = rotation
        if (alpha == 0f) {
            alpha = 1f
        }
    }

    public fun reset() {
        val newRotation = if (image.rotation < 180) 0f else 360f
        image.animate().setDuration(1000).rotation(newRotation)
        animate().setDuration(1000).alpha(0f).setStartDelay(1000)
    }
}
