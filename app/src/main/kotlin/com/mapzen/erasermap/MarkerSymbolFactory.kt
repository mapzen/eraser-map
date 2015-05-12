package com.mapzen.erasermap

import android.content.Context
import org.oscim.android.canvas.AndroidGraphics
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol

public class MarkerSymbolFactory(val context: Context) {
    public fun getDefaultMarker(): MarkerSymbol {
        return AndroidGraphics.makeMarker(context.getResources()
                .getDrawable(R.drawable.ic_pin), MarkerItem.HotspotPlace.BOTTOM_CENTER)
    }

    public fun getActiveMarker(): MarkerSymbol {
        return AndroidGraphics.makeMarker(context.getResources()
                .getDrawable(R.drawable.ic_pin_active), MarkerItem.HotspotPlace.BOTTOM_CENTER)
    }
}
