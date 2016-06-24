package com.mapzen.erasermap.model

import com.mapzen.erasermap.R
import com.mapzen.valhalla.TravelType
import org.json.JSONObject

class MultiModalHelper(private val rawRoute: JSONObject?) {

    fun getTransitIcon(type: TravelType): Int {
        when (type) {
            TravelType.TRAM -> return R.drawable.ic_transit_type_0
            TravelType.METRO -> return R.drawable.ic_transit_type_1
            TravelType.RAIL -> return R.drawable.ic_transit_type_2
            TravelType.BUS -> return R.drawable.ic_transit_type_3
            TravelType.FERRY -> return R.drawable.ic_transit_type_4
            TravelType.CABLE_CAR -> return R.drawable.ic_transit_type_5
            TravelType.GONDOLA -> return R.drawable.ic_transit_type_6
            TravelType.FUNICULAR -> return R.drawable.ic_transit_type_7
            else -> return 0
        }
    }


}
