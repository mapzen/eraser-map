package com.mapzen.erasermap.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.ListRowItem
import com.mapzen.erasermap.model.ListRowType
import com.mapzen.erasermap.model.MultiModalHelper
import com.mapzen.erasermap.util.DisplayHelper
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.TransitStop
import com.mapzen.valhalla.TravelMode
import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.concurrent.TimeUnit

class MultiModalDirectionListAdapter(val context: Context, val instructionGrouper: InstructionGrouper,
    val reverse : Boolean?, val multiModalHelper: MultiModalHelper) : BaseAdapter() {

  companion object {
    const val NUM_VIEW_TYPES = 3
    const val INSTRUCTION_TYPE_DESTINATION = 4
    const val INSTRUCTION_TYPE_DESTINATION_RIGHT = 5
    const val INSTRUCTION_TYPE_DESTINATION_LEFT = 6
    const val VIEW_TYPE_INSTRUCTION_ROW = 0
    const val VIEW_TYPE_TRANSIT_ROW = 1
  }

  private val INSTRUCTION_TYPES_NOT_SHOWN = HashSet<Int>()

  private val TRAVEL_MODE_TO_ICON = HashMap<TravelMode, Int>()
  private val TRAVEL_MODE_TO_LAYOUT_ID = HashMap<TravelMode, Int>()
  private val TRAVEL_MODE_TO_ITEM_TYPE = HashMap<TravelMode, ListRowType>()

  lateinit var listItems: ArrayList<ListRowItem>

  val recycler = ViewRecycler()

  init {
    INSTRUCTION_TYPES_NOT_SHOWN.add(INSTRUCTION_TYPE_DESTINATION)
    INSTRUCTION_TYPES_NOT_SHOWN.add(INSTRUCTION_TYPE_DESTINATION_RIGHT)
    INSTRUCTION_TYPES_NOT_SHOWN.add(INSTRUCTION_TYPE_DESTINATION_LEFT)

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
          holder = PedestrianViewHolder(view, context)
        }
        ListRowType.TRANSIT -> {
          holder = TransitViewHolder(view, context)
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
    var prevMode: TravelMode? = null
    for (i in 0..instructionGrouper.numGroups() - 1) {
      val instructionGroup = instructionGrouper.getInstructionGroup(i)
      val listItem = listItemForInstructionGroup(instructionGroup)
      listItem.prevMode = prevMode
      if (i <= instructionGrouper.numGroups() - 2) {
        val nextGroup = instructionGrouper.getInstructionGroup(i+1)
        listItem.nextItemMode = nextGroup.travelMode
      }
      listItems.add(listItem)
      prevMode = instructionGroup.travelMode
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
    val index = instructionGroup.instructions.size-1
    val item = ListRowItem(ListRowType.ARRIVAL, R.layout.icon_title_row)
    val instruction = instructionGroup.instructions[index]
    item.iconId = DisplayHelper.getRouteDrawable(context, instruction.turnInstruction)
    item.title = instruction.getHumanTurnInstruction()
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
    holder.totalTimeView.timeInMinutes = TimeUnit.SECONDS.toMinutes(
        instructionGroup.totalTime.toLong()).toInt()
    if (listItem.expanded) {
      holder.distanceTimeContainer.openArrow()
      holder.instructionsContainer.visibility = View.VISIBLE
    } else {
      holder.distanceTimeContainer.closeArrow()
      holder.instructionsContainer.visibility = View.GONE
    }
    holder.distanceTimeContainer.setOnClickListener {
      listItem.expanded = !listItem.expanded
      notifyDataSetChanged()
    }

    for (i in 0..holder.instructionsContainer.childCount - 1) {
      recycler.queueView(holder.instructionsContainer.getChildAt(i), VIEW_TYPE_INSTRUCTION_ROW)
    }
    holder.instructionsContainer.removeAllViews()
    if (listItem.expanded) {
      for (instruction in instructionGroup.instructions) {
        if (INSTRUCTION_TYPES_NOT_SHOWN.contains(instruction.turnInstruction)) {
          continue
        }
        val distance = instruction.distance
        var instructionRow = recycler.dequeueView(VIEW_TYPE_INSTRUCTION_ROW)
        if (instructionRow == null) {
          instructionRow = View.inflate(context, R.layout.instruction_row, null)
        }

        var iconId = DisplayHelper.getRouteDrawable(context, instruction.turnInstruction)
        if (instruction.getTravelMode() == TravelMode.TRANSIT) {
          iconId = multiModalHelper.getTransitIcon(instructionGroup.travelType)
        }

        val iconView = instructionRow?.findViewById(R.id.icon) as ImageView
        val titleView = instructionRow?.findViewById(R.id.title) as TextView
        val distanceView = instructionRow?.findViewById(R.id.distance) as DistanceView

        iconView.setImageResource(iconId)
        titleView.text = instruction.getHumanTurnInstruction()
        distanceView.distanceInMeters = distance

        holder.instructionsContainer.addView(instructionRow)
      }
    }

  }

  private fun setTransitRow(position: Int, holder: TransitViewHolder) {
    val listItem = listItems[position]
    val instructionGroup = listItem.extra as InstructionGroup
    val instruction = instructionGroup.instructions[0]

    setTransitRowTextViewsAndIcons(holder, instructionGroup, instruction)

    if (listItem.expanded) {
      holder.distanceTimeContainer.openArrow()
      holder.stationNamesContainer.visibility = View.VISIBLE
    } else {
      holder.distanceTimeContainer.closeArrow()
      holder.stationNamesContainer.visibility = View.GONE
    }

    holder.distanceTimeContainer.setOnClickListener {
      listItem.expanded = !listItem.expanded
      notifyDataSetChanged()
    }

    holder.transitLine.setBackgroundColor(instructionGroup.transitColor)
    
    setPrevTransitLines(holder, listItem, position)
    setEndingTransitInfo(holder, listItem, instruction)
    setTransitStationNamesContainer(holder, listItem, instruction)
  }

  fun setTransitRowTextViewsAndIcons(holder: TransitViewHolder, instructionGroup: InstructionGroup,
      instruction: Instruction) {
    holder.startingStationName.text = instructionGroup.firstStationName(context, instruction)
    holder.travelTypeIcon.setImageResource(multiModalHelper.getTransitIcon(
        instruction.getTravelType()))
    holder.instructionText.text = instructionGroup.transitInstructionSpannable(instruction)
    holder.distanceTimeText.text = instructionGroup.numberOfStops(context, instruction)
    holder.timeView.timeInMinutes = TimeUnit.SECONDS.toMinutes(
        instructionGroup.totalTime.toLong()).toInt()
  }

  fun setPrevTransitLines(holder: TransitViewHolder, listItem: ListRowItem, position: Int) {
    if (listItem.prevMode != null) {
      if (listItem.prevMode == TravelMode.TRANSIT) {
        val prevListItem = listItems[position-1]
        val prevInstructionGroup = prevListItem.extra as InstructionGroup
        holder.prevTransitLine.setBackgroundColor(prevInstructionGroup.transitColor)
        holder.prevTransitLine.visibility = View.VISIBLE
        holder.prevPedestrianLine.visibility = View.GONE
      } else {
        holder.prevTransitLine.visibility = View.GONE
        holder.prevPedestrianLine.visibility = View.VISIBLE
      }
    } else {
      holder.prevTransitLine.visibility = View.GONE
    }
  }

  fun setEndingTransitInfo(holder: TransitViewHolder, listItem: ListRowItem,
      instruction: Instruction) {
    val transitStops = instruction.getTransitInfo()?.getTransitStops() as ArrayList<TransitStop>
    // we show all stops except first and last which are shown in larger font
    var numStops = transitStops.size - 2
    if (listItem.nextItemMode != null) {
      if (listItem.nextItemMode == TravelMode.PEDESTRIAN) {
        holder.endingStationDot.visibility = View.VISIBLE
        holder.endingStationName.visibility = View.VISIBLE
        holder.pedestrianConnector.visibility = View.VISIBLE
      } else {
        numStops++
        holder.endingStationDot.visibility = View.GONE
        holder.endingStationName.visibility = View.GONE
        holder.pedestrianConnector.visibility = View.GONE
      }
    }
    holder.endingStationName.text = transitStops[transitStops.size-1].getName()
  }

  fun setTransitStationNamesContainer(holder: TransitViewHolder, listItem: ListRowItem,
      instruction: Instruction) {
    val transitStops = instruction.getTransitInfo()?.getTransitStops() as ArrayList<TransitStop>
    // we show all stops except first and last which are shown in larger font
    var numStops = transitStops.size - 2

    for (i in 0..holder.stationNamesContainer.childCount - 1) {
      recycler.queueView(holder.stationNamesContainer.getChildAt(i), VIEW_TYPE_TRANSIT_ROW)
    }
    holder.stationNamesContainer.removeAllViews()

    if (listItem.expanded) {
      for (i in 1..numStops) {
        var stationRow = recycler.dequeueView(VIEW_TYPE_TRANSIT_ROW)
        if (stationRow == null) {
          stationRow = View.inflate(context, R.layout.transit_station_row, null)
        }
        val stationName = stationRow?.findViewById(R.id.station_name) as TextView
        stationName.text = transitStops[i].getName()
        holder.stationNamesContainer.addView(stationRow)
      }
    }
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

  class PedestrianViewHolder(view: View, context: Context): ViewHolder() {
    val totalDistanceView: DistanceView
    val totalTimeView: TimeView
    val instructionsContainer: LinearLayout
    val dashedLine: View
    val distanceTimeContainer: DistanceTimeExpanderLayout

    init {
      totalDistanceView = view.findViewById(R.id.total_distance) as DistanceView
      totalTimeView = view.findViewById(R.id.total_time) as TimeView
      instructionsContainer = view.findViewById(R.id.instructions_container) as LinearLayout
      dashedLine = view.findViewById(R.id.dashed_line)
      distanceTimeContainer = view.findViewById(R.id.distance_time_container)
          as DistanceTimeExpanderLayout
      val textView = view.findViewById(R.id.distance_time_text_view) as TextView
      val builder = StringBuilder()
      builder.append(context.getString(R.string.comma))
      builder.append(" ")
      textView.text = builder.toString()
    }
  }

  class TransitViewHolder(view: View, context: Context): ViewHolder() {
    val transitContainer: View
    val startingStationName: TextView
    val travelTypeIcon: ImageView
    val instructionText: TextView
    val distanceTimeText: TextView
    val timeView: TimeView
    val stationNamesContainer: LinearLayout
    val distanceTimeContainer: DistanceTimeExpanderLayout
    val transitLine: View
    val endingStationName: TextView
    val endingStationDot: View
    val pedestrianConnector: View
    val prevTransitLine: View
    val prevPedestrianLine: View

    init {
      transitContainer = view
      startingStationName = view.findViewById(R.id.starting_station_name) as TextView
      travelTypeIcon = view.findViewById(R.id.travel_type_icon) as ImageView
      instructionText = view.findViewById(R.id.instruction_text) as TextView
      distanceTimeText = view.findViewById(R.id.distance_time_text_view) as TextView
      timeView = view.findViewById(R.id.total_time) as TimeView
      stationNamesContainer = view.findViewById(R.id.station_names_container) as LinearLayout
      distanceTimeContainer = view.findViewById(R.id.distance_time_container)
          as DistanceTimeExpanderLayout
      transitLine = view.findViewById(R.id.transit_line)
      endingStationName = view.findViewById(R.id.ending_station_name) as TextView
      endingStationDot = view.findViewById(R.id.ending_dot)
      pedestrianConnector = view.findViewById(R.id.pedestrian_connector)
      prevTransitLine = view.findViewById(R.id.prev_transit_line)
      prevPedestrianLine = view.findViewById(R.id.prev_pedestrian_line)
      view.findViewById(R.id.total_distance).visibility = View.GONE
      val scale = context.resources.displayMetrics.density
      distanceTimeText.setPadding((scale * 8).toInt(), distanceTimeText.paddingTop,
          distanceTimeText.paddingRight, distanceTimeText.paddingBottom)
    }

  }

}
