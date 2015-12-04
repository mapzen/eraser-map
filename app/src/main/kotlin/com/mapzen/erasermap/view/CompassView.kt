package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import com.mapzen.erasermap.R

public class CompassView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    val background: ImageView by lazy { findViewById(R.id.background) as ImageView }
    val compass: ImageView by lazy { findViewById(R.id.compass) as ImageView }

    init {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(R.layout.view_compass, this, true)
    }

    override fun setRotation(rotation: Float) {
        compass.rotation = rotation
        if (alpha == 0f) {
            alpha = 1f
        }
    }

    public fun reset() {
        compass.animate().setDuration(1000).rotation(0f)
        animate().setDuration(1000).alpha(0f).setStartDelay(1000)
    }
}
