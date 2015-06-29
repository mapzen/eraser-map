package com.mapzen.erasermap.model

import com.mapzen.pelias.gson.Feature
import com.mapzen.valhalla.Instruction
import java.util.ArrayList

data class RouteModeEvent(val destination: Feature, val instructions: ArrayList<Instruction>)
