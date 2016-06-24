package com.mapzen.erasermap.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.InstructionGrouper
import com.mapzen.erasermap.model.MultiModalHelper
import com.mapzen.erasermap.util.DisplayHelper
import com.mapzen.valhalla.TravelMode

class RoutingDirectionListAdapter(val context: Context, val instructionGrouper: InstructionGrouper,
    val reverse : Boolean?, val multiModalHelper: MultiModalHelper) : SectionBaseAdapter(),
    SectionAdapterInterface, SectionViewAdapterInterface {

  private final val CURRENT_LOCATION_OFFSET =  1

  var currentInstructionIndex: Int = -1

  override fun numSections(): Int {
    return instructionGrouper.numGroups() + CURRENT_LOCATION_OFFSET
  }

  override fun numRowsForSection(section: Int): Int {
    if (reverse != null && !reverse) {
      if (section == 0) {
        return 1
      }
      return instructionGrouper.numInstructionsInGroup(section - CURRENT_LOCATION_OFFSET)
    } else {
      if (section == instructionGrouper.numGroups()) {
        return 1
      } else {
        return instructionGrouper.numInstructionsInGroup(section)
      }
    }

  }

  //TODO
  override fun viewForSection(section: Int, convertView: View?, parent: ViewGroup?): View {
    val view = View.inflate(context, R.layout.direction_list_item, null)
    val textView = view.findViewById(R.id.simple_instruction) as TextView
    textView.text = "$section"
    return view
  }

  override fun viewForRow(position: Int, section: Int, row: Int, convertView: View?,
      parent: ViewGroup?): View {
    val view: View?
    val holder: ViewHolder
    if (convertView == null) {
      view = View.inflate(context, R.layout.direction_list_item, null)
      holder = ViewHolder(view)
      view.tag = holder
    } else {
      view = convertView
      holder = view.tag as ViewHolder
    }

    if (reverse == true) {
      setReversedDirectionListItem(position, section, row, holder)
    } else {
      setDirectionListItem(position, section, row, holder)
    }

    if (position == currentInstructionIndex + CURRENT_LOCATION_OFFSET) {
      view?.setBackgroundColor(context.resources.getColor(R.color.light_gray))
    } else {
      view?.setBackgroundColor(context.resources.getColor(android.R.color.white))
    }

    return view as View
  }

  private fun setReversedDirectionListItem(position : Int, section: Int, row: Int,
      holder: ViewHolder)  {
    if (section == instructionGrouper.numGroups()) {
      setListItemToCurrentLocation(holder)
    } else {
      val instructionGroup = instructionGrouper.getInstructionGroup(section)
      val distance = instructionGroup.distances[row]
      var iconId: Int = DisplayHelper.getRouteDrawable(context, instructionGroup.types[row])

      if (instructionGroup.travelMode == TravelMode.TRANSIT) {
        iconId = multiModalHelper.getTransitIcon(instructionGroup.travelType)
      }

      holder.simpleInstruction.text = instructionGroup.strings[row].toString()
      holder.distanceView.distanceInMeters = distance
      holder.iconImageView.setImageResource(iconId)
    }
  }

  private fun setDirectionListItem(position : Int, section: Int, row: Int, holder: ViewHolder) {
    if (section == 0) {
      setListItemToCurrentLocation(holder)
    } else {
      val adjustedSection = section - CURRENT_LOCATION_OFFSET
      val instructionGroup = instructionGrouper.getInstructionGroup(adjustedSection)
      val distance = instructionGroup.distances[row]
      var iconId = DisplayHelper.getRouteDrawable(context, instructionGroup.types[row])

      if (instructionGroup.travelMode == TravelMode.TRANSIT) {
        iconId = multiModalHelper.getTransitIcon(instructionGroup.travelType)
      }

      holder.simpleInstruction.text = instructionGroup.strings[row].toString()
      holder.distanceView.distanceInMeters = distance
      holder.iconImageView.setImageResource(iconId)
    }
  }

  private fun setListItemToCurrentLocation(holder: ViewHolder) {
    holder.simpleInstruction.setText(R.string.current_location)
    holder.distanceView.text = ""
    holder.iconImageView.setImageResource(R.drawable.ic_locate)
  }

  class ViewHolder(view: View) {
    val simpleInstruction: TextView
    val distanceView: DistanceView
    val iconImageView: ImageView

    init {
      simpleInstruction = view.findViewById(R.id.simple_instruction) as TextView
      distanceView = view.findViewById(R.id.distance) as DistanceView
      iconImageView = view.findViewById(R.id.icon) as ImageView
    }
  }
}
