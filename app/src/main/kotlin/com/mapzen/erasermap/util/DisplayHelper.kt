package com.mapzen.erasermap.util

import android.content.Context

public object DisplayHelper {

    /**
     * Fetch the resource drawable ID of the turn icon for the given instruction and style.
     * @param context current context in which to display the icon.
     * @param turnInstruction the integer value representing this turn instruction.
     * @return the resource ID of the turn icon to display.
     */
    public fun getRouteDrawable(context: Context, turnInstruction: Int?): Int {
        return context.getResources().getIdentifier("ic_route_"
                + turnInstruction, "drawable", context.getPackageName())
    }
}
