package com.mapzen.erasermap.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.RouteManager
import com.mapzen.pelias.SimpleFeature
import java.util.ArrayList
import javax.inject.Inject

public class RouteDirectionListActivity : AppCompatActivity() {
    var routeManager: RouteManager? = null
        @Inject set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as EraserMapApplication).component()?.inject(this)
        setContentView(R.layout.activity_instructions)
        supportActionBar?.hide()
        val listView = findViewById(R.id.instruction_list_view) as ListView
        findViewById(R.id.route_reverse).visibility = View.GONE

        val instructions = routeManager?.route?.getRouteInstructions()
        val strings = ArrayList<String>()
        val types = ArrayList<Int>()
        val distances = ArrayList<Int>()
        val destination = SimpleFeature.fromFeature(routeManager?.destination).name()

        if (instructions != null) {
            for(instruction in instructions) {
                val humanInstruction = instruction.getHumanTurnInstruction()
                strings.add(humanInstruction ?: "")
                types.add(instruction.turnInstruction)
                distances.add(instruction.distance)
            }
        }

        setHeaderOrigins(destination, false)
        listView.adapter = DirectionListAdapter(this, strings, types, distances, false)
    }

    private fun setHeaderOrigins(destination: String, reverse: Boolean) {
        val startTextView = findViewById(R.id.starting_point) as TextView
        val destinationTextView = findViewById(R.id.destination) as TextView
        if (reverse) {
            startTextView.text = destination
            destinationTextView.setText(R.string.current_location)
        } else {
            startTextView.setText(R.string.current_location)
            destinationTextView.text = destination
        }
    }
}
