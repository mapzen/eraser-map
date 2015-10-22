package com.mapzen.erasermap.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import com.mapzen.erasermap.R
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
        val reverse : Boolean? = bundle?.getBoolean("reverse", true)
        setHeaderOrigins(bundle, reverse)
        if (instruction_strings != null) {
            listView.setAdapter(DirectionListAdapter(this, instruction_strings, instruction_types, instruction_distances, reverse))
            listView.setOnItemClickListener { parent, view, position, id ->
                setResult(position)
                finish()
            }
        }
    }

    private fun setHeaderOrigins(bundle: Bundle?, reverse: Boolean?) {
        if (reverse == true) {
            (findViewById(R.id.starting_point) as TextView).setText(bundle?.getString("destination"))
            (findViewById(R.id.destination) as TextView).setText(R.string.current_location)
        } else {
            (findViewById(R.id.starting_point) as TextView).setText(R.string.current_location)
            (findViewById(R.id.destination) as TextView).setText(bundle?.getString("destination"))
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        setResult(-1)
        finish()
        return true
    }
}
