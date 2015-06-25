package com.mapzen.erasermap.view

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.ImageView
import com.mapzen.erasermap.R
import com.mapzen.erasermap.util.DisplayHelper
import com.mapzen.pelias.SimpleFeature
import com.mapzen.valhalla.Instruction
import com.mapzen.helpers.DistanceFormatter
import java.util.ArrayList

public class InstructionListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)
        getSupportActionBar()?.hide()
        val listView = findViewById(R.id.instruction_list_view) as ListView
        findViewById(R.id.route_reverse).setVisibility(View.GONE)
        val bundle = getIntent()?.getExtras()
        val instruction_strings: ArrayList<String>? = bundle?.getStringArrayList("instruction_strings")
        val instruction_types: ArrayList<Int>? = bundle?.getIntegerArrayList("instruction_types")
        val instruction_distances: ArrayList<Int>? = bundle?.getIntegerArrayList("instruction_distances")
        val reverse : Boolean = bundle?.getBoolean("reverse") as Boolean
        setHeaderOrigins(bundle, reverse)
        if (instruction_strings != null) {
            listView.setAdapter(DirectionListAdapter(this, instruction_strings, instruction_types, instruction_distances, reverse))
            listView.setOnItemClickListener { parent, view, position, id ->
                setResult(position)
                finish()
            }
        }
    }

    private fun setHeaderOrigins(bundle: Bundle?, reverse: Boolean) {
        if (reverse) {
            (findViewById(R.id.starting_point) as TextView).setText(bundle?.getString("destination"))
            (findViewById(R.id.destination) as TextView).setText(R.string.current_location)
            findViewById(R.id.starting_location_icon).setVisibility(View.GONE)
            findViewById(R.id.destination_location_icon).setVisibility(View.VISIBLE)
        } else {
            (findViewById(R.id.starting_point) as TextView).setText(R.string.current_location)
            (findViewById(R.id.destination) as TextView).setText(bundle?.getString("destination"))
            findViewById(R.id.starting_location_icon).setVisibility(View.VISIBLE)
            findViewById(R.id.destination_location_icon).setVisibility(View.GONE)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        setResult(-1)
        finish()
        return true
    }

    private class DirectionListAdapter(context: Context, strings: ArrayList<String>?,
                                       types: ArrayList<Int>?, distances: ArrayList<Int>?,
                                       reverse : Boolean) : BaseAdapter() {
        private final var CURRENT_LOCATION_OFFSET =  1
        private var instruction_strings: ArrayList<String>? = strings
        private var instruction_types: ArrayList<Int>? = types
        private var instruction_distances: ArrayList<Int>? = distances
        private var context: Context  = context
        private var reverse : Boolean = reverse

        override fun getCount(): Int {
            var size = if (instruction_strings != null) (instruction_strings!!.size()
                    + CURRENT_LOCATION_OFFSET) else 0
            return size
         }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getItem(position: Int): Any? {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var view: View = View.inflate(context, R.layout.direction_list_item, null)
            if(reverse) {
                setReversedDirectionListItem(position, view)
            } else {
                setDirectionListItem(position, view)
            }
            return view
        }

        private fun setReversedDirectionListItem(position : Int, view : View)  {
            if(position == instruction_strings?.size()) {
                setListItemToCurrentLocation(view)
            } else {
                var distanceVal : Int? = instruction_distances?.get(position)
                var formattedDistance : String = DistanceFormatter.format(
                        (distanceVal?.toInt() as Int))
                var iconId : Int = DisplayHelper.getRouteDrawable(context,
                        instruction_types?.get(position))

                (view.findViewById(R.id.simple_instruction) as TextView).setText(
                        instruction_strings?.get(position).toString())
                (view.findViewById(R.id.distance) as TextView).setText(formattedDistance)
                (view.findViewById(R.id.icon) as ImageView).setImageResource(iconId)
            }
        }

        private fun setDirectionListItem(position : Int, view : View) {
            if (position == 0 ) {
                setListItemToCurrentLocation(view)
            } else {
                var distanceVal : Int? = instruction_distances?.get(position - CURRENT_LOCATION_OFFSET)
                var formattedDistance : String = DistanceFormatter.format((distanceVal?.toInt() as Int))
                var iconId : Int = DisplayHelper.getRouteDrawable(context,
                        instruction_types?.get(position - CURRENT_LOCATION_OFFSET))

                (view.findViewById(R.id.simple_instruction) as TextView).setText(
                        instruction_strings?.get(position - CURRENT_LOCATION_OFFSET).toString())
                (view.findViewById(R.id.distance) as TextView).setText(formattedDistance)
                (view.findViewById(R.id.icon) as ImageView).setImageResource(iconId)
            }
        }

        private fun setListItemToCurrentLocation(view : View) {
            (view.findViewById(R.id.simple_instruction) as TextView).setText(R.string.current_location)
            (view.findViewById(R.id.icon) as ImageView).setImageResource(R.drawable.ic_locate)
        }
    }

}
