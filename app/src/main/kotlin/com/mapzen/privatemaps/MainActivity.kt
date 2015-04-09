package com.mapzen.privatemaps

import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.view.Menu
import android.view.MenuItem
import org.oscim.android.MapView
import org.oscim.backend.AssetAdapter
import org.oscim.layers.tile.buildings.BuildingLayer
import org.oscim.layers.tile.vector.labeling.LabelLayer
import org.oscim.theme.ThemeFile
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource
import java.io.InputStream

public class MainActivity : ActionBarActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapView = findViewById(R.id.map) as MapView;
        val baseLayer = mapView.map().setBaseMap(OSciMap4TileSource("https://vector.mapzen.com/osm/all"));
        mapView.map().layers().add(BuildingLayer(mapView.map(), baseLayer));
        mapView.map().layers().add(LabelLayer(mapView.map(), baseLayer));
        mapView.map().setTheme(MapzenTheme());
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.getItemId()

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    class MapzenTheme : ThemeFile {
        override fun getRenderThemeAsStream(): InputStream? {
            return AssetAdapter.readFileAsStream("styles/mapzen.xml");
        }
    }
}
