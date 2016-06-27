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

  companion object {
    const val SECTION_CURR_LOCATION = 0
    const val NUM_ROWS_CURR_LOCATION = 0

    const val VIEW_TYPE_CURRENT_LOCATION_SECTION = 0
    const val VIEW_TYPE_PEDESTRIAN_SECTION = 1
    const val VIEW_TYPE_PADDING_SECTION = 2
    const val VIEW_TYPE_PEDESTRIAN_ROW = 3
    const val VIEW_TYPE_TRANSIT_ROW = 4
    const val NUM_VIEW_TYPES = VIEW_TYPE_TRANSIT_ROW + 1
  }

  var currentInstructionIndex: Int = -1

  override fun numSections(): Int {
    return instructionGrouper.numGroups() + CURRENT_LOCATION_OFFSET
  }

  override fun numRowsForSection(section: Int): Int {
    if (reverse != null && !reverse) {
      if (section == SECTION_CURR_LOCATION) {
        return NUM_ROWS_CURR_LOCATION
      }
      return instructionGrouper.numInstructionsInGroup(section - CURRENT_LOCATION_OFFSET)
    } else {
      if (section == instructionGrouper.numGroups()) {
        return NUM_ROWS_CURR_LOCATION
      } else {
        return instructionGrouper.numInstructionsInGroup(section)
      }
    }

  }

  override fun getItemViewType(position: Int): Int {
    if (isSection(position)) {
      val section = sectionCalculator.positionToSection[position] as Int
      if (isCurrentLocationSection(section)) {
        return VIEW_TYPE_CURRENT_LOCATION_SECTION
      } else if(isPedestrianSection(section)) {
        return VIEW_TYPE_PEDESTRIAN_SECTION
      } else {
        return VIEW_TYPE_PADDING_SECTION
      }
    } else {
      val section = sectionCalculator.positionsToSection[position] as Int
      if (isPedestrianSection(section)) {
        return VIEW_TYPE_PEDESTRIAN_ROW
      } else {
        return VIEW_TYPE_TRANSIT_ROW
      }
    }
  }

  override fun getViewTypeCount(): Int {
    return NUM_VIEW_TYPES
  }

  override fun viewForSection(section: Int, convertView: View?, parent: ViewGroup?): View {
    var view: View?

    if (isCurrentLocationSection(section)) {
      view = View.inflate(context, R.layout.current_location_section, null)
    } else if (isPedestrianSection(section)) {
      view = View.inflate(context, R.layout.pedestrian_section, null)
    } else {
      view = View.inflate(context, R.layout.padding_section, null)
    }

    return view!!
  }

  override fun viewForRow(position: Int, section: Int, row: Int, convertView: View?,
      parent: ViewGroup?): View {
    val view: View?
    val holder: ViewHolder

    if (convertView == null) {
      if (isPedestrianSection(section)) {
        view = View.inflate(context, R.layout.pedestrian_direction_row, null)
        holder = PedestrianViewHolder(view)
      } else {
        view = View.inflate(context, R.layout.transit_direction_row, null)
        holder = TransitViewHolder(view)
      }
      view.tag = holder
    } else {
      view = convertView
      holder = view.tag as ViewHolder
    }

    if (isPedestrianSection(section)) {
      setPedestrianRow(section, row, holder as PedestrianViewHolder)
    } else {
      setTransitRow(section, row, holder as TransitViewHolder)
    }


    return view as View
  }

  private fun isSection(position: Int): Boolean {
    return sectionCalculator.positionToSection.contains(position)
  }

  private fun isCurrentLocationSection(section: Int): Boolean {
    return reverse == false && section == SECTION_CURR_LOCATION || reverse == true
        && section == instructionGrouper.numGroups()
  }

  private fun isPedestrianSection(section: Int): Boolean {
    return instructionGrouper.getInstructionGroup(
        adjustedSection(section)).travelMode == TravelMode.PEDESTRIAN
  }

  private fun adjustedSection(section: Int): Int {
    if (reverse == false) {
      return section - CURRENT_LOCATION_OFFSET
    }
    return section
  }

  private fun setPedestrianRow(section: Int, row: Int, holder: PedestrianViewHolder) {
    val instructionGroup = instructionGrouper.getInstructionGroup(adjustedSection(section))
    val distance = instructionGroup.distances[row]
    var iconId = DisplayHelper.getRouteDrawable(context, instructionGroup.types[row])

    if (instructionGroup.travelMode == TravelMode.TRANSIT) {
      iconId = multiModalHelper.getTransitIcon(instructionGroup.travelType)
    }

    holder.simpleInstruction.text = instructionGroup.strings[row].toString()
    holder.distanceView.distanceInMeters = distance
    holder.iconImageView.setImageResource(iconId)
  }

  private fun setTransitRow(section: Int, row: Int, holder: TransitViewHolder) {
    val instructionGroup = instructionGrouper.getInstructionGroup(adjustedSection(section))

    holder.subtitle.text = instructionGroup.strings[row].toString()
  }

  open class ViewHolder() {
  }

  class PedestrianViewHolder(view: View): ViewHolder() {
    val simpleInstruction: TextView
    val distanceView: DistanceView
    val iconImageView: ImageView

    init {
      simpleInstruction = view.findViewById(R.id.simple_instruction) as TextView
      distanceView = view.findViewById(R.id.distance) as DistanceView
      iconImageView = view.findViewById(R.id.icon) as ImageView
    }
  }

  class TransitViewHolder(view: View): ViewHolder() {
    val title: TextView
    val subtitle: TextView
    val tertiaryTitle: TextView

    init {
      title = view.findViewById(R.id.title) as TextView
      subtitle = view.findViewById(R.id.subtitle) as TextView
      tertiaryTitle = view.findViewById(R.id.tertiary_title) as TextView
    }
  }

}
