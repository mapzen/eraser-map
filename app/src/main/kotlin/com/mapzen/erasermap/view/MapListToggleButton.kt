package com.mapzen.erasermap.view

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.mapzen.erasermap.R

public class MapListToggleButton(context: Context?, attrs: AttributeSet?) :
        ImageView(context, attrs) {

    enum class MapListState {
        MAP,
        LIST
    }

    public var state: MapListState = MapListState.LIST
        set(value) {
            if (value == MapListState.MAP) {
                setImageResource(R.drawable.ic_map)
            } else {
                setImageResource(R.drawable.ic_list)
            }

            field = value
        }
}
