package com.mapzen.erasermap.view

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.erasermap.util.DisplayHelper
import java.util.ArrayList

class DirectionListAdapter(val context: Context, val strings: ArrayList<String>?,
        val types: ArrayList<Int>?, val distances: ArrayList<Int>?,
        val reverse : Boolean?, val showCurrentLocation : Boolean = true) : BaseAdapter() {

    private final var CURRENT_LOCATION_OFFSET =  1

    var currentInstructionIndex: Int = 0

    var directionItemClickListener: DirectionItemClickListener? = null

    override fun getCount(): Int {
        val size = strings?.size ?: 0
        return if (showCurrentLocation) size + CURRENT_LOCATION_OFFSET else size
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

        if (!showCurrentLocation) {
            setSimpleDirectionListItem(position, holder)
        } else if(reverse == true) {
            setReversedDirectionListItem(position, holder)
        } else {
            setDirectionListItem(position, holder)
        }

        if (position == currentInstructionIndex) {
            view?.setBackgroundColor(context.resources.getColor(R.color.light_gray))
        } else {
            view?.setBackgroundColor(context.resources.getColor(android.R.color.white))
        }

        return view
    }

    private fun setReversedDirectionListItem(position : Int, holder: ViewHolder)  {
        if(position == strings?.size) {
            setListItemToCurrentLocation(holder)
        } else {
            val distance = distances?.get(position) ?: 0
            val iconId: Int = DisplayHelper.getRouteDrawable(context, types?.get(position))

            holder.simpleInstruction.text = strings?.get(position).toString()
            holder.distanceView.distanceInMeters = distance
            holder.iconImageView.setImageResource(iconId)
        }
    }

    private fun setDirectionListItem(position : Int, holder: ViewHolder) {
        if (position == 0) {
            setListItemToCurrentLocation(holder)
        } else {
            var distance = distances?.get(position - CURRENT_LOCATION_OFFSET) ?: 0
            var iconId = DisplayHelper.getRouteDrawable(context,
                    types?.get(position - CURRENT_LOCATION_OFFSET))

            holder.simpleInstruction.text = strings?.get(position - CURRENT_LOCATION_OFFSET).toString()
            holder.distanceView.distanceInMeters = distance
            holder.iconImageView.setImageResource(iconId)
        }
    }

    private fun setSimpleDirectionListItem(position: Int, holder: ViewHolder) {
        var distance = distances?.get(position) ?: 0
        var iconId = DisplayHelper.getRouteDrawable(context, types?.get(position))
        holder.simpleInstruction.text = strings?.get(position).toString()
        holder.distanceView.distanceInMeters = distance
        holder.iconImageView.setImageResource(iconId)
    }

    private fun setListItemToCurrentLocation(holder: ViewHolder) {
        holder.simpleInstruction.setText(R.string.current_location)
        holder.iconImageView.setImageResource(R.drawable.ic_locate)
    }

    private fun onDirectionClicked(position: Int) {
        directionItemClickListener?.onDirectionItemClicked(position)
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
