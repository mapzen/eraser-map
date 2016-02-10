package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.RelativeLayout
import com.mapzen.erasermap.R

/**
 * Generic class for buttons that need an icon centered on bg
 */
public abstract class ButtonView(context: Context, attrs: AttributeSet) : RelativeLayout(context, attrs) {

    public abstract fun idForLayout():Int
    public abstract fun idForImage():Int

    val background: ImageView by lazy { findViewById(R.id.background) as ImageView }
    val image: ImageView by lazy { findViewById(idForImage()) as ImageView }

    init {
        (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
                .inflate(idForLayout(), this, true)
    }
}