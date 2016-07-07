package com.mapzen.erasermap.model

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.StyleSpan
import com.mapzen.erasermap.R
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.TransitInfo
import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import java.util.ArrayList
import java.util.regex.Pattern


class InstructionGroup(val travelType: TravelType, val travelMode: TravelMode,
    val instructions: ArrayList<Instruction>) {

  val totalDistance: Int by lazy { calculateTotalDistance() }
  val totalTime: Int by lazy { calculateTotalTime() }
  private var firstStationName: String? = null
  private var numberOfStops: String? = null
  private var transitInstruction: SpannableString? = null
  val transitColor: Int by lazy { calculateTransitColor() }

  private fun calculateTotalDistance(): Int {
    var total = 0
    for(instruction in instructions) {
      total+= instruction.distance
    }
    return total
  }

  private fun calculateTotalTime(): Int {
    var total = 0
    for(instruction in instructions) {
      total+= instruction.getTime()
    }
    return total
  }

  fun firstStationName(context: Context, instruction: Instruction): String? {
    if (firstStationName == null) {
      val transitStop = instruction.getTransitInfo()?.getTransitStops()?.get(0)
      if (transitStop != null) {
        val builder = StringBuilder()
        builder.append(transitStop?.getName())
        builder.append(" ")
        builder.append(context.getString(R.string.station))
        firstStationName = builder.toString()
      }
    }
    return firstStationName
  }

  fun numberOfStops(context: Context, instruction: Instruction): String? {
    if (numberOfStops == null) {
      val stops = instruction.getTransitInfo()?.getTransitStops()
      if (stops != null) {
        val numStops = stops?.size - 1
        val builder = StringBuilder()
        builder.append(numStops)
        builder.append(" ")
        builder.append(context.getString(R.string.stops))
        builder.append(context.getString(R.string.comma))
        numberOfStops = builder.toString()
      }
    }
    return numberOfStops
  }

  fun transitInstructionSpannable(instruction: Instruction): SpannableString? {
    if (transitInstruction == null) {
      var turnInstruction = instruction.getHumanTurnInstruction()
      val pattern = Pattern.compile("\\([0-9]+ stops\\)")
      val matcher = pattern.matcher(turnInstruction)
      turnInstruction = matcher.replaceAll("")
      val spannableString = SpannableString(turnInstruction)
      val transitInfo = instruction.getTransitInfo() as TransitInfo
      val shortnameLoc = spannableString.indexOf(transitInfo.getShortName(), 0, true)
      val headsignLoc = spannableString.indexOf(transitInfo.getHeadsign(), 0 , true)
      spannableString.setSpan(StyleSpan(Typeface.BOLD), shortnameLoc,
          shortnameLoc + transitInfo.getShortName().length, 0)
      spannableString.setSpan(StyleSpan(Typeface.BOLD), headsignLoc,
          headsignLoc + transitInfo.getHeadsign().length, 0)
      transitInstruction = spannableString
    }
    return transitInstruction
  }

  fun calculateTransitColor(): Int {
    val instruction = instructions[0]
    try {
      return Color.parseColor(instruction.getTransitInfoColorHex())
    } catch(e : IllegalArgumentException) {
      return R.color.mz_white
    }
  }

}
