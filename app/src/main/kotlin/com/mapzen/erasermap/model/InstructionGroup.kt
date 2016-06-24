package com.mapzen.erasermap.model

import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import java.util.ArrayList


class InstructionGroup(val strings: ArrayList<String>,
    val types: ArrayList<Int>, val distances: ArrayList<Int>,
    val travelType: TravelType, val travelMode: TravelMode) {

}