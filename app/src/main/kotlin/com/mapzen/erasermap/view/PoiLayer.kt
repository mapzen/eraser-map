package com.mapzen.erasermap.view

import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import org.oscim.core.GeoPoint
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.map.Map

public class PoiLayer(val map: Map, val defaultMarker: MarkerSymbol, val activeMarker: MarkerSymbol)
        : ItemizedLayer<MarkerItem>(map, defaultMarker), ItemizedLayer.OnItemGestureListener<MarkerItem> {

    public var onPoiClickListener: OnPoiClickListener? = null

    init {
        map.layers().add(this)
        setOnItemGestureListener(this)
    }

    public fun addAll(features: List<Feature>) {
        for (feature in features) {
            val simpleFeature = SimpleFeature.fromFeature(feature)
            addItem(MarkerItem(simpleFeature.getProperty(SimpleFeature.ID),
                    simpleFeature.getTitle(),
                    simpleFeature.getAddress(),
                    GeoPoint(simpleFeature.getLat(), simpleFeature.getLon())))
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

    override fun onItemSingleTapUp(index: Int, item: MarkerItem?): Boolean {
        onPoiClickListener?.onPoiClick(index)
        return true;
    }

    override fun onItemLongPress(index: Int, item: MarkerItem?): Boolean {
        return true;
    }

    public trait OnPoiClickListener {
        public fun onPoiClick(position: Int)
    }
}
