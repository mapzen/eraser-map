package com.mapzen.erasermap.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.MultiModalHelper
import com.mapzen.erasermap.util.DisplayHelper
import com.mapzen.valhalla.TravelMode
import com.mapzen.valhalla.TravelType
import java.util.ArrayList

/**
 * Adapter to show list of directions with first element current location
 */
class DirectionListAdapter(val context: Context, val strings: ArrayList<String>,
        val types: ArrayList<Int>, val distances: ArrayList<Int>,
        val travelTypes: ArrayList<TravelType>, val travelModes: ArrayList<TravelMode>,
        val reverse : Boolean?) : BaseAdapter() {

    private final val CURRENT_LOCATION_OFFSET =  1

    var currentInstructionIndex: Int = -1

    var directionItemClickListener: DirectionItemClickListener? = null

    override fun getCount(): Int {
        return strings.size + CURRENT_LOCATION_OFFSET
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    override fun getItem(position: Int): Any? {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
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
        view?.setOnClickListener { onDirectionClicked(position) }

        if (reverse == true) {
            setReversedDirectionListItem(position, holder)
        } else {
            setDirectionListItem(position, holder)
        }

        if (position == currentInstructionIndex + CURRENT_LOCATION_OFFSET) {
            view?.setBackgroundColor(context.resources.getColor(R.color.light_gray))
        } else {
            view?.setBackgroundColor(context.resources.getColor(android.R.color.white))
        }

        return view
    }

    private fun setReversedDirectionListItem(position : Int, holder: ViewHolder)  {
        if (position == strings.size) {
            setListItemToCurrentLocation(holder)
        } else {
            val distance = distances[position]
            var iconId: Int = DisplayHelper.getRouteDrawable(context, types[position])

            holder.simpleInstruction.text = strings[position].toString()
            holder.distanceView.distanceInMeters = distance
            holder.iconImageView.setImageResource(iconId)
        }
    }

    private fun setDirectionListItem(position : Int, holder: ViewHolder) {
        if (position == 0) {
            setListItemToCurrentLocation(holder)
        } else {
            val adjustedPosition = position - CURRENT_LOCATION_OFFSET
            val distance = distances[adjustedPosition]
            var iconId = DisplayHelper.getRouteDrawable(context, types[adjustedPosition])

            holder.simpleInstruction.text = strings[adjustedPosition].toString()
            holder.distanceView.distanceInMeters = distance
            holder.iconImageView.setImageResource(iconId)
        }
    }

    private fun setListItemToCurrentLocation(holder: ViewHolder) {
        holder.simpleInstruction.setText(R.string.current_location)
        holder.distanceView.text = ""
        holder.iconImageView.setImageResource(R.drawable.ic_locate)
    }

    private fun onDirectionClicked(position: Int) {
        directionItemClickListener?.onDirectionItemClicked(position-CURRENT_LOCATION_OFFSET)
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
