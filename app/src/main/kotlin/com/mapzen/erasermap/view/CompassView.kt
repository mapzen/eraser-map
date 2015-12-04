package com.mapzen.erasermap.view

import android.animation.ObjectAnimator
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
    }

    public fun reset() {
        val start = compass.rotation
        val finish = 0f
        val animator = ObjectAnimator.ofFloat(compass, "rotation", start, finish);
        animator.setDuration(1000)
        animator.start()
    }
}
