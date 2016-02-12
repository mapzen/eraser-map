package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import com.mapzen.erasermap.R

/**
 * Draws a bg and mute icon for use in route mode
 */
class MuteView(context: Context, attrs: AttributeSet) : ButtonView(context, attrs) {

    override fun onFinishInflate() {
        super.onFinishInflate()
        image.isSelected = true
    }

    override fun idForLayout(): Int {
        return R.layout.view_mute
    }

    override fun idForImage(): Int {
        return R.id.mute
    }

    public fun setMuted(muted: Boolean) {
        image.isSelected = muted
    }
}
