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

public class DirectionListAdapter(val context: Context, val strings: ArrayList<String>?,
        val types: ArrayList<Int>?, val distances: ArrayList<Int>?,
        val reverse : Boolean?, val showCurrentLocation : Boolean = true) : BaseAdapter() {

    private final var CURRENT_LOCATION_OFFSET =  1

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
        val view = View.inflate(context, R.layout.direction_list_item, null)
        if (!showCurrentLocation) {
            setSimpleDirectionListItem(position, view)
        } else if(reverse == true) {
            setReversedDirectionListItem(position, view)
        } else {
            setDirectionListItem(position, view)
        }

        return view
    }

    private fun setReversedDirectionListItem(position : Int, view : View)  {
        if(position == strings?.size()) {
            setListItemToCurrentLocation(view)
        } else {
            val distance = distances?.get(position) ?: 0
            val iconId: Int = DisplayHelper.getRouteDrawable(context, types?.get(position))

            (view.findViewById(R.id.simple_instruction) as TextView).text =
                    strings?.get(position).toString()
            (view.findViewById(R.id.distance) as DistanceView).distanceInMeters = distance
            (view.findViewById(R.id.icon) as ImageView).setImageResource(iconId)
        }
    }

    private fun setDirectionListItem(position : Int, view : View) {
        if (position == 0) {
            setListItemToCurrentLocation(view)
        } else {
            var distance = distances?.get(position - CURRENT_LOCATION_OFFSET) ?: 0
            var iconId = DisplayHelper.getRouteDrawable(context,
                    types?.get(position - CURRENT_LOCATION_OFFSET))

            (view.findViewById(R.id.simple_instruction) as TextView).text =
                    strings?.get(position - CURRENT_LOCATION_OFFSET).toString()
            (view.findViewById(R.id.distance) as DistanceView).distanceInMeters = distance
            (view.findViewById(R.id.icon) as ImageView).setImageResource(iconId)
        }
    }

    private fun setSimpleDirectionListItem(position: Int, view: View) {
        var distance = distances?.get(position) ?: 0
        var iconId = DisplayHelper.getRouteDrawable(context, types?.get(position))
        (view.findViewById(R.id.simple_instruction) as TextView).text =
                strings?.get(position).toString()
        (view.findViewById(R.id.distance) as DistanceView).distanceInMeters = distance
        (view.findViewById(R.id.icon) as ImageView).setImageResource(iconId)
    }

    private fun setListItemToCurrentLocation(view : View) {
        (view.findViewById(R.id.simple_instruction) as TextView).setText(R.string.current_location)
        (view.findViewById(R.id.icon) as ImageView).setImageResource(R.drawable.ic_locate)
    }
}
