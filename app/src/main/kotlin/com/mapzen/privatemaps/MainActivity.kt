package com.mapzen.privatemaps

import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.mapburrito.MapController
import com.mapzen.pelias.Pelias
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.pelias.widget.AutoCompleteAdapter
import com.mapzen.pelias.widget.AutoCompleteListView
import com.mapzen.pelias.widget.PeliasSearchView
import com.squareup.okhttp.HttpResponseCache
import org.oscim.android.MapView
import org.oscim.android.canvas.AndroidGraphics
import org.oscim.layers.marker.ItemizedLayer
import org.oscim.layers.marker.MarkerItem
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.tiling.source.OkHttpEngine
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import javax.inject.Inject

public class MainActivity : AppCompatActivity(), ViewController {
    private val BASE_TILE_URL = "http://vector.dev.mapzen.com/osm/all"
    private val STYLE_PATH = "styles/mapzen.xml"
    private val FIND_ME_ICON = android.R.drawable.star_big_on
    private val LOCATION_UPDATE_INTERVAL_IN_MS = 1000L
    private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT = 0f

    var locationClient: LostApiClient? = null
    [Inject] set
    var tileCache: HttpResponseCache? = null
    [Inject] set
    var savedSearch: SavedSearch? = null
    [Inject] set
    var presenter: MainPresenter? = null
    [Inject] set

    var app: PrivateMapsApplication? = null
    var mapController: MapController? = null
    var autoCompleteAdapter: AutoCompleteAdapter? = null
    var optionsMenu: Menu? = null
    var poiLayer: ItemizedLayer<MarkerItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = getApplication() as PrivateMapsApplication
        app?.component()?.inject(this)
        presenter?.viewController = this
        locationClient?.connect()
        initMapController()
        initPoiLayer()
        initAutoCompleteAdapter()
        initFindMeButton()
        centerOnCurrentLocation()
        presenter?.restoreViewState()
    }

    override fun onStart() {
        super<AppCompatActivity>.onStart()
        savedSearch?.deserialize(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SavedSearch.TAG, null))
    }

    override fun onResume() {
        super<AppCompatActivity>.onResume()
        initLocationUpdates()
    }

    override fun onPause() {
        super<AppCompatActivity>.onPause()
        locationClient?.disconnect()
    }

    override fun onStop() {
        super<AppCompatActivity>.onStop()
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(SavedSearch.TAG, savedSearch?.serialize())
                .commit();
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()
        saveCurrentSearchTerm()
    }

    private fun initMapController() {
        val mapView = findViewById(R.id.map) as MapView
        mapController = MapController(mapView.map())
                .setHttpEngine(OkHttpEngine.OkHttpFactory(tileCache))
                .setApiKey(BuildConfig.VECTOR_TILE_API_KEY)
                .setTileSource(BASE_TILE_URL)
                .addBuildingLayer()
                .addLabelLayer()
                .setTheme(STYLE_PATH)
                .setCurrentLocationDrawable(getResources().getDrawable(FIND_ME_ICON))
    }

    private fun initPoiLayer() {
        val map = mapController?.getMap()
        if (map != null) {
            poiLayer = ItemizedLayer<MarkerItem>(map, getDefaultMarkerSymbol())
            map.layers().add(poiLayer)
        }
    }

    private fun initAutoCompleteAdapter() {
        autoCompleteAdapter = AutoCompleteAdapter(this, R.layout.list_item_auto_complete)
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
        optionsMenu = menu

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search),
                SearchOnActionExpandListener())

        val searchView = menu.findItem(R.id.action_search).getActionView()
        val listView = findViewById(R.id.auto_complete) as AutoCompleteListView
        val emptyView = findViewById(android.R.id.empty)

        if (searchView is PeliasSearchView) {
            listView.setAdapter(autoCompleteAdapter)
            val pelias = Pelias.getPelias();
            pelias.setLocationProvider(MapLocationProvider(mapController))
            searchView.setAutoCompleteListView(listView)
            searchView.setSavedSearch(savedSearch)
            searchView.setPelias(Pelias.getPelias())
            searchView.setCallback(PeliasCallback())
            searchView.setOnSubmitListener({ presenter?.onQuerySubmit() })
            listView.setEmptyView(emptyView)
            restoreCurrentSearchTerm()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.getItemId()
        when (id) {
            R.id.action_settings -> return true
            R.id.action_search -> return true
            R.id.action_clear -> {
                savedSearch?.clear()
                autoCompleteAdapter?.clear()
                autoCompleteAdapter?.notifyDataSetChanged()
                return true
            }
        }

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    private fun saveCurrentSearchTerm() {
        val menuItem = optionsMenu?.findItem(R.id.action_search)
        val actionView = menuItem?.getActionView() as PeliasSearchView
        if (menuItem!!.isActionViewExpanded()) {
            presenter?.currentSearchTerm = actionView.getQuery().toString()
        }
    }

    private fun restoreCurrentSearchTerm() {
        val menuItem = optionsMenu?.findItem(R.id.action_search)
        val actionView = menuItem?.getActionView() as PeliasSearchView
        val term = presenter?.currentSearchTerm
        if (term != null) {
            menuItem?.expandActionView()
            actionView.setQuery(term, false)
            if (findViewById(R.id.search_results).getVisibility() == View.VISIBLE) {
                actionView.clearFocus()
            }
            presenter?.currentSearchTerm = null
        }
    }

    inner class PeliasCallback : Callback<Result> {
        private val TAG: String = "PeliasCallback";

        override fun success(result: Result?, response: Response?) {
            presenter?.onSearchResultsAvailable(result)
        }

        override fun failure(error: RetrofitError?) {
            Log.e(TAG, "Error fetching search results: " + error?.getMessage())
        }
    }

    inner class SearchOnActionExpandListener : MenuItemCompat.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
            return true
        }

        override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
            presenter?.onCollapseSearchView()
            return true
        }
    }

    class MapLocationProvider(val mapController: MapController?) : PeliasLocationProvider {
        override fun getLat(): String? {
            return mapController?.getMap()?.getMapPosition()?.getLatitude().toString()
        }

        override fun getLon(): String? {
            return mapController?.getMap()?.getMapPosition()?.getLongitude().toString()
        }
    }

    override fun showSearchResults(features: List<Feature>) {
        showSearchResultsPager(features)
        addSearchResultsToMap(features)
    }

    private fun showSearchResultsPager(features: List<Feature>) {
        val pager = findViewById(R.id.search_results) as SearchResultsView
        pager.setAdapter(SearchResultsAdapter(this, features))
        pager.setVisibility(View.VISIBLE)
    }

    private fun addSearchResultsToMap(features: List<Feature>) {
        poiLayer?.removeAllItems()
        for (feature in features) {
            poiLayer?.addItem(SimpleFeature.fromFeature(feature).getMarker())
        }
        centerOnCurrentFeature(features)
    }

    private fun centerOnCurrentFeature(features: List<Feature>) {
        Handler().postDelayed(Runnable {
            val pager = findViewById(R.id.search_results) as SearchResultsView
            val current = SimpleFeature.fromFeature(features.get(pager.getCurrentItem()));
            val location = Location("map");
            location.setLatitude(current.getLat())
            location.setLongitude(current.getLon())
            mapController?.resetMapAndCenterOn(location)
            mapController?.getMap()?.updateMap(true)
        }, 100);
    }

    override fun hideSearchResults() {
        hideSearchResultsPager()
        removeSearchResultsFromMap()
    }

    private fun hideSearchResultsPager() {
        (findViewById(R.id.search_results) as SearchResultsView).setVisibility(View.GONE)
    }

    private fun removeSearchResultsFromMap() {
        poiLayer?.removeAllItems()
        mapController?.getMap()?.updateMap(true)
    }

    override fun showProgress() {
        findViewById(R.id.progress).setVisibility(View.VISIBLE)
    }

    override fun hideProgress() {
        findViewById(R.id.progress).setVisibility(View.GONE)
    }

    private fun getDefaultMarkerSymbol(): MarkerSymbol {
        return AndroidGraphics.makeMarker(getResources().getDrawable(R.drawable.ic_pin),
                MarkerItem.HotspotPlace.BOTTOM_CENTER);
    }
}
