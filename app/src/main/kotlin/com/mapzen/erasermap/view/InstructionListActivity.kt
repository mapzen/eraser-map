package com.mapzen.erasermap.view

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature
import com.mapzen.valhalla.Instruction
import java.util.*

public class InstructionListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instructions)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)

        val listView = findViewById(R.id.instruction_list_view) as ListView
      //  val headerView = View.inflate(this, R.layout.list_header_search_results, null)
        //val title = headerView.findViewById(R.id.title) as TextView
        val bundle = getIntent()?.getExtras()
    //    val query = bundle?.getString("query")
        val instructions: ArrayList<String>? = bundle?.getStringArrayList("instructions")

        if (instructions != null) {
            listView.setAdapter(ArrayAdapter(this, R.layout.list_item_instruction, instructions))
            listView.setOnItemClickListener { parent, view, position, id ->
                setResult(position)
                finish()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        setResult(-1)
        finish()
        return true
    }
}
