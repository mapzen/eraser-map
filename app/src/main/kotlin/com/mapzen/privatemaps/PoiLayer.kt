package com.mapzen.privatemaps

import com.mapzen.pelias.gson.Feature
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.map.Map;

public class PoiLayer(val map: Map, val defaultMarker: MarkerSymbol, val activeMarker: MarkerSymbol)
        : ItemizedLayer<MarkerItem>(map, defaultMarker) {
    init {
        map.layers().add(this)
    }

    public fun addAll(features: List<Feature>) {
        for (feature in features) {
            addItem(SimpleFeature.fromFeature(feature).getMarker())
        }
    }

    public fun resetAllItems() {
        for (item in mItemList) {
            item.setMarker(defaultMarker)
        }
    }

    public fun setActiveItem(position: Int) {
        mItemList.get(position).setMarker(activeMarker)
    }
}
