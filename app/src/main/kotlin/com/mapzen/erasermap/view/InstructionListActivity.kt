package com.mapzen.erasermap.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.mapzen.erasermap.R

public class InstructionListActivity : AppCompatActivity() {
    companion object {
        @JvmStatic val EXTRA_STRINGS = "instruction_strings"
        @JvmStatic val EXTRA_TYPES = "instruction_types"
        @JvmStatic val EXTRA_DISTANCES = "instruction_distances"
        @JvmStatic val EXTRA_REVERSE = "reverse"
        @JvmStatic val EXTRA_DESTINATION = "destination"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)
        supportActionBar?.hide()
        val listView = findViewById(R.id.instruction_list_view) as ListView
        findViewById(R.id.route_reverse).visibility = View.GONE
        val bundle = intent?.extras
        val strings = bundle?.getStringArrayList(EXTRA_STRINGS)
        val types = bundle?.getIntegerArrayList(EXTRA_TYPES)
        val distances = bundle?.getIntegerArrayList(EXTRA_DISTANCES)
        val destination = bundle?.getString(EXTRA_DESTINATION) ?: getString(R.string.destination)
        val reverse = bundle?.getBoolean(EXTRA_REVERSE, true) ?: false
        setHeaderOrigins(destination, reverse)
        if (strings != null) {
            listView.adapter = DirectionListAdapter(this, strings, types, distances, reverse)
            listView.setOnItemClickListener { parent, view, position, id ->
                setResult(position)
                finish()
            }
        }
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

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        setResult(-1)
        finish()
        return true
    }
}
