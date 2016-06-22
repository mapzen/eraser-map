package com.mapzen.erasermap.model

import com.mapzen.erasermap.R
import org.json.JSONObject

class MultiModalHelper(private val rawRoute: JSONObject?) {

    companion object {
        const val KEY_TRIP = "trip"
        const val KEY_LEGS = "legs"
        const val KEY_MANEUVERS = "maneuvers"
        const val KEY_TRAVEL_MODE = "travel_mode"
        const val KEY_TRAVEL_TYPE = "travel_type"
    }

    fun getTravelMode(position: Int): TravelMode {
        if (rawRoute == null) {
            return TravelMode.DRIVE
        }

        val mode = rawRoute.getJSONObject(KEY_TRIP)
                .getJSONArray(KEY_LEGS)
                .getJSONObject(0)
                .getJSONArray(KEY_MANEUVERS)
                .getJSONObject(position)
                .getString(KEY_TRAVEL_MODE)

        when (mode) {
            TravelMode.DRIVE.toString() -> return TravelMode.DRIVE
            TravelMode.PEDESTRIAN.toString() -> return TravelMode.PEDESTRIAN
            TravelMode.BICYCLE.toString() -> return TravelMode.BICYCLE
            TravelMode.TRANSIT.toString() -> return TravelMode.TRANSIT
            else -> return TravelMode.DRIVE
        }
    }

    fun getTravelType(position: Int): TravelType {
        if (rawRoute == null) {
            return TravelType.CAR
        }

        val type = rawRoute.getJSONObject(KEY_TRIP)
                .getJSONArray(KEY_LEGS)
                .getJSONObject(0)
                .getJSONArray(KEY_MANEUVERS)
                .getJSONObject(position)
                .getString(KEY_TRAVEL_TYPE)

        when (type) {
            TravelType.CAR.toString() -> return TravelType.CAR
            TravelType.FOOT.toString() -> return TravelType.FOOT
            TravelType.ROAD.toString() -> return TravelType.ROAD
            TravelType.TRAM.toString() -> return TravelType.TRAM
            TravelType.METRO.toString() -> return TravelType.METRO
            TravelType.RAIL.toString() -> return TravelType.RAIL
            TravelType.BUS.toString() -> return TravelType.BUS
            TravelType.FERRY.toString() -> return TravelType.FERRY
            TravelType.CABLE_CAR.toString() -> return TravelType.CABLE_CAR
            TravelType.GONDOLA.toString() -> return TravelType.GONDOLA
            TravelType.FUNICULAR.toString() -> return TravelType.FUNICULAR
            else -> return TravelType.CAR
        }
    }

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

    enum class TravelMode(private val mode: String) {
        DRIVE("drive"),
        PEDESTRIAN("pedestrian"),
        BICYCLE("bicycle"),
        TRANSIT("transit");

        override fun toString(): String {
            return mode
        }
    }

    enum class TravelType(private val type: String) {
        CAR("car"), // Drive
        FOOT("foot"), // Pedestrian
        ROAD("road"), // Bicycle
        TRAM("tram"), // Tram or light rail
        METRO("metro"), // Metro or subway
        RAIL("rail"),
        BUS("bus"),
        FERRY("ferry"),
        CABLE_CAR("cable_car"),
        GONDOLA("gondola"),
        FUNICULAR("funicular");

        override fun toString(): String {
            return type
        }
    }
}
