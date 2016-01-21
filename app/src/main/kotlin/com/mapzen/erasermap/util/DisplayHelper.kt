package com.mapzen.erasermap.util

import android.content.Context

public object DisplayHelper {
    @JvmStatic private val ROUTE_ICON_PREFIX = "ic_route_"
    @JvmStatic private val ROUTE_ICON_POSTFIX_LARGE = "_lg"
    @JvmStatic private val DRAWABLE_PREFIX = "drawable"

    /**
     * Fetch the resource drawable ID of the turn icon for the given instruction and style.
     * @param context current context in which to display the icon.
     * @param type the integer value representing this maneuver type.
     * @param isLarge the size of the asset to be loaded.
     * @return the resource ID of the turn icon to display.
     */
    public fun getRouteDrawable(context: Context, type: Int?, isLarge: Boolean = false): Int {
        val baseName = ROUTE_ICON_PREFIX + type
        val name = if (isLarge) baseName + ROUTE_ICON_POSTFIX_LARGE else baseName
        return context.resources.getIdentifier(name, DRAWABLE_PREFIX, context.packageName)
    }
}
