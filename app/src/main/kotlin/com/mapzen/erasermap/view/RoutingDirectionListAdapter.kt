package com.mapzen.erasermap.view

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.InstructionGroup
import com.mapzen.erasermap.model.InstructionGrouper
import com.mapzen.erasermap.model.ListRowItem
import com.mapzen.erasermap.model.ListRowType
import com.mapzen.erasermap.model.MultiModalHelper
import com.mapzen.erasermap.util.DisplayHelper
import com.mapzen.valhalla.TravelMode
import java.util.ArrayList
import java.util.HashMap

class RoutingDirectionListAdapter(val context: Context, val instructionGrouper: InstructionGrouper,
    val reverse : Boolean?, val multiModalHelper: MultiModalHelper) : BaseAdapter() {

  companion object {
    const val SEC_PER_MIN = 60
    const val NUM_VIEW_TYPES = 3
  }

  private val TRAVEL_MODE_TO_ICON = HashMap<TravelMode, Int>()
  private val TRAVEL_MODE_TO_LAYOUT_ID = HashMap<TravelMode, Int>()
  private val TRAVEL_MODE_TO_ITEM_TYPE = HashMap<TravelMode, ListRowType>()

  lateinit var listItems: ArrayList<ListRowItem>

  init {
    TRAVEL_MODE_TO_ICON.put(TravelMode.PEDESTRIAN, R.drawable.ic_pedestrian)
    TRAVEL_MODE_TO_ICON.put(TravelMode.TRANSIT, R.drawable.ic_current_location)

    TRAVEL_MODE_TO_LAYOUT_ID.put(TravelMode.PEDESTRIAN, R.layout.pedestrian_direction_row)
    TRAVEL_MODE_TO_LAYOUT_ID.put(TravelMode.TRANSIT, R.layout.transit_direction_row)

    TRAVEL_MODE_TO_ITEM_TYPE.put(TravelMode.PEDESTRIAN, ListRowType.PEDESTRIAN)
    TRAVEL_MODE_TO_ITEM_TYPE.put(TravelMode.TRANSIT, ListRowType.TRANSIT)

    listItems = ArrayList<ListRowItem>()
    prepareListItems()
  }

  override fun getItem(position: Int): Any? {
    return 0
  }

  override fun getItemId(position: Int): Long {
    return 0
  }

  override fun getCount(): Int {
    return listItems.size
  }

  override fun getItemViewType(position: Int): Int {
    when (listItems[position].layoutId) {
      R.layout.icon_title_row -> return 0
      R.layout.pedestrian_direction_row -> return 1
      R.layout.transit_direction_row -> return 2
    }
    return 0
  }

  override fun getViewTypeCount(): Int {
    return NUM_VIEW_TYPES
  }

  override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
    val view: View
    val holder: ViewHolder
    val listItem = listItems[position]

    if (convertView == null) {
      view = View.inflate(context, listItem.layoutId, null)
      when (listItem.type) {
        ListRowType.CURRENT_LOCATION,
        ListRowType.ARRIVAL -> {
          holder = IconTitleViewHolder(view)
        }
        ListRowType.PEDESTRIAN -> {
          holder = PedestrianViewHolder(view)
        }
        ListRowType.TRANSIT -> {
          holder = TransitViewHolder(view)
        }
      }
      view.tag = holder
    } else {
      view = convertView
      holder = view.tag as ViewHolder
    }

    when (listItem.type) {
      ListRowType.CURRENT_LOCATION,
      ListRowType.ARRIVAL -> {
        setIconTitleRow(position, holder as IconTitleViewHolder)
      }
      ListRowType.PEDESTRIAN -> {
        setPedestrianRow(position, holder as PedestrianViewHolder)
      }
      ListRowType.TRANSIT -> {
        setTransitRow(position, holder as TransitViewHolder)
      }
    }

    return view
  }

  private fun prepareListItems() {
    if (reverse == false) {
      listItems.add(listItemForCurrentLocation())
    }
    for (i in 0..instructionGrouper.numGroups() - 1) {
      listItems.add(listItemForInstructionGroup(instructionGrouper.getInstructionGroup(i)))
    }
    val lastInstructionGroup = instructionGrouper.getInstructionGroup(
        instructionGrouper.numGroups() - 1)
    listItems.add(listItemForLastInstruction(lastInstructionGroup))
    if (reverse == true) {
      listItems.add(listItemForCurrentLocation())
    }
  }

  private fun listItemForCurrentLocation(): ListRowItem {
    val listItem = ListRowItem(ListRowType.CURRENT_LOCATION, R.layout.icon_title_row)
    listItem.iconId = R.drawable.ic_locate
    listItem.title = context.getString(R.string.current_location)

    return listItem
  }

  private fun listItemForInstructionGroup(instructionGroup: InstructionGroup): ListRowItem {
    val travelMode = instructionGroup.travelMode
    val item = ListRowItem(TRAVEL_MODE_TO_ITEM_TYPE[travelMode] as ListRowType,
        TRAVEL_MODE_TO_LAYOUT_ID[travelMode] as Int)
    item.iconId = TRAVEL_MODE_TO_ICON[travelMode]
    item.extra = instructionGroup
    return item
  }

  private fun listItemForLastInstruction(instructionGroup: InstructionGroup): ListRowItem {
    val index = instructionGroup.strings.size-1
    val item = ListRowItem(ListRowType.ARRIVAL, R.layout.icon_title_row)
    item.iconId = DisplayHelper.getRouteDrawable(context, instructionGroup.types[index])
    item.title = instructionGroup.strings[index]
    return item
  }

  private fun setIconTitleRow(position: Int, holder: IconTitleViewHolder) {
    val item = listItems[position]
    holder.titleView.text = item.title
    holder.iconView.setImageResource(item.iconId as Int)
  }

  private fun setPedestrianRow(position: Int, holder: PedestrianViewHolder) {
    val listItem = listItems[position]
    val instructionGroup = listItem.extra as InstructionGroup
    holder.totalDistanceView.distanceInMeters = instructionGroup.totalDistance
    holder.totalTimeView.timeInMinutes = instructionGroup.totalTime / SEC_PER_MIN

    holder.instructionsContainer.removeAllViews()

    for (i in 0..instructionGroup.strings.size - 1) { //TODO: recycle views added to this
      val distance = instructionGroup.distances[i]
      val instructionRow = View.inflate(context, R.layout.instruction_row, null)
      var iconId = DisplayHelper.getRouteDrawable(context, instructionGroup.types[i])
      if (instructionGroup.travelMode == TravelMode.TRANSIT) {
        iconId = multiModalHelper.getTransitIcon(instructionGroup.travelType)
      }

      val iconView = instructionRow.findViewById(R.id.icon) as ImageView
      val titleView = instructionRow.findViewById(R.id.title) as TextView
      val distanceView = instructionRow.findViewById(R.id.distance) as DistanceView

      iconView.setImageResource(iconId)
      titleView.text = instructionGroup.strings[i].toString()
      distanceView.distanceInMeters = distance

      holder.instructionsContainer.addView(instructionRow)
    }
  }

  //TODO:
  private fun setTransitRow(position: Int, holder: TransitViewHolder) {
    val listItem = listItems[position]
    val instructionGroup = listItem.extra as InstructionGroup
    holder.subtitle.text = instructionGroup.strings[0].toString()
  }

  open class ViewHolder() {
  }

  class IconTitleViewHolder(view: View): ViewHolder() {
    val titleView: TextView
    val iconView: ImageView

    init {
      titleView = view.findViewById(R.id.title) as TextView
      iconView = view.findViewById(R.id.image_view) as ImageView
    }
  }

  class PedestrianViewHolder(view: View): ViewHolder() {
    val totalDistanceView: DistanceView
    val totalTimeView: TimeView
    val instructionsContainer: LinearLayout
    val dashedLine: View

    init {
      totalDistanceView = view.findViewById(R.id.total_distance) as DistanceView
      totalTimeView = view.findViewById(R.id.total_time) as TimeView
      instructionsContainer = view.findViewById(R.id.instructions_container) as LinearLayout
      dashedLine = view.findViewById(R.id.dashed_line)
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
