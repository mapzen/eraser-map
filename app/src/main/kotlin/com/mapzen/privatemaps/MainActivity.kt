package com.mapzen.privatemaps

import android.location.Location
import android.os.Bundle
import android.support.v7.app.ActionBarActivity
import android.view.Menu
import android.view.MenuItem
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.mapburrito.MapController
import org.oscim.android.MapView

public class MainActivity : ActionBarActivity() {
    private val BASE_TILE_URL = "https://vector.mapzen.com/osm/all"
    private val STYLE_PATH = "styles/mapzen.xml"
    private val FIND_ME_ICON = android.R.drawable.star_big_on
    private val LOCATION_UPDATE_INTERVAL_IN_MS = 1000L
    private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT = 0f

    var mapController : MapController? = null
    var mapView : MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        LostApiClient.Builder(this).build().connect()
        initMap()
        initFindMeButton()
        initLocationUpdates()
    }

    private fun initMap() {
        mapView = findViewById(R.id.map) as MapView
        mapController = MapController(mapView?.map())
                .setTileSource(BASE_TILE_URL)
                .addBuildingLayer()
                .addLabelLayer()
                .setTheme(STYLE_PATH)
                .setCurrentLocationDrawable(getResources().getDrawable(FIND_ME_ICON))

        centerOnCurrentLocation()
    }

    private fun initFindMeButton() {
        findViewById(R.id.find_me).setOnClickListener({ centerOnCurrentLocation() })
    }

    private fun initLocationUpdates() {
        val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL_IN_MS)
                .setSmallestDisplacement(LOCATION_UPDATE_SMALLEST_DISPLACEMENT)

        LocationServices.FusedLocationApi.requestLocationUpdates(locationRequest) {
            location: Location ->  mapController?.showCurrentLocation(location)
            mapView?.map()?.updateMap(true)
        }
    }

    private fun centerOnCurrentLocation() {
        val location = LocationServices.FusedLocationApi.getLastLocation()
        if (location != null) {
            mapController?.showCurrentLocation(location)?.resetMapAndCenterOn(location)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        getMenuInflater().inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}
