package com.mapzen.erasermap.view

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.mapzen.erasermap.R
import com.mapzen.pelias.SimpleFeature
import java.util.ArrayList

public class SearchResultsListActivity : HomeAsUpActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_results)

        val listView = findViewById(R.id.search_results_list_view) as ListView
        val bundle = intent?.extras
        val simpleFeatures: ArrayList<SimpleFeature>? = bundle?.getParcelableArrayList("features")

        if (simpleFeatures != null) {
            listView.adapter = SearchListAdapter(this, R.layout.list_item_search_results, simpleFeatures)
            listView.setOnItemClickListener { parent, view, position, id ->
                setResult(position)
                finish()
            }
        }
    }

    /**
     * Adapter for SimpleFeatures that uses label() rather than toString() for display text.
     */
    class SearchListAdapter(context: Context, resource: Int, val objects: List<SimpleFeature>) :
            ArrayAdapter<SimpleFeature>(context, resource, objects) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
            val view = super.getView(position, convertView, parent) as TextView
            view.text = objects[position].label()
            return view
        }
    }
}
