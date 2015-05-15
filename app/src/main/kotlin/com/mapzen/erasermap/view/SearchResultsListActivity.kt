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
import java.util.ArrayList

public class SearchResultsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true)

        val listView = findViewById(R.id.search_results_list_view) as ListView
        val headerView = View.inflate(this, R.layout.list_header_search_results, null)
        val title = headerView.findViewById(R.id.title) as TextView
        val bundle = getIntent()?.getExtras()
        val query = bundle?.getString("query")
        val simpleFeatures: ArrayList<SimpleFeature>? = bundle?.getParcelableArrayList("features")

        if (query != null) {
            title.setText("\"" + query + "\"")
            listView.addHeaderView(headerView, null, false)
            listView.setHeaderDividersEnabled(false)
        }

        if (simpleFeatures != null) {
            listView.setAdapter(ArrayAdapter(this, R.layout.list_item_search_results, simpleFeatures))
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
