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
import com.squareup.okhttp.HttpResponseCache
import org.oscim.android.MapView
import org.oscim.tiling.source.OkHttpEngine
import javax.inject.Inject

public class MainActivity : ActionBarActivity() {
    private val BASE_TILE_URL = "https://vector.mapzen.com/osm/all"
    private val STYLE_PATH = "styles/mapzen.xml"
    private val FIND_ME_ICON = android.R.drawable.star_big_on
    private val LOCATION_UPDATE_INTERVAL_IN_MS = 1000L
    private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT = 0f

    var mapController: MapController? = null
    var locationClient: LostApiClient? = null
    [Inject] set
    var tileCache: HttpResponseCache? = null
    [Inject] set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        (getApplication() as PrivateMapsApplication).component().inject(this)
        locationClient?.connect()
        initMapController()
        initFindMeButton()
        centerOnCurrentLocation()
    }

    override fun onPause() {
        super.onPause()
        locationClient?.disconnect()
    }

    override fun onResume() {
        super.onResume()
        initLocationUpdates()
    }

    private fun initMapController() {
        val mapView = findViewById(R.id.map) as MapView
        mapController = MapController(mapView.map())
                .setHttpEngine(OkHttpEngine.OkHttpFactory(tileCache))
                .setTileSource(BASE_TILE_URL)
                .addBuildingLayer()
                .addLabelLayer()
                .setTheme(STYLE_PATH)
                .setCurrentLocationDrawable(getResources().getDrawable(FIND_ME_ICON))
    }

    private fun initFindMeButton() {
        findViewById(R.id.find_me).setOnClickListener({ centerOnCurrentLocation() })
    }

    private fun initLocationUpdates() {
        if (locationClient?.isConnected() == false) {
            locationClient?.connect()
        }

        val locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(LOCATION_UPDATE_INTERVAL_IN_MS)
                .setSmallestDisplacement(LOCATION_UPDATE_SMALLEST_DISPLACEMENT)

        LocationServices.FusedLocationApi?.requestLocationUpdates(locationRequest) {
            location: Location ->  mapController?.showCurrentLocation(location)?.update()
        }
    }

    private fun centerOnCurrentLocation() {
        val location = LocationServices.FusedLocationApi?.getLastLocation()
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
