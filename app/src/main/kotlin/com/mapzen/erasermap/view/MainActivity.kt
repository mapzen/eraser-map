package com.mapzen.erasermap.view

import android.content.Intent
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
import android.widget.Toast
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.mapburrito.MapController
import com.mapzen.pelias.Pelias
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.pelias.widget.AutoCompleteAdapter
import com.mapzen.pelias.widget.AutoCompleteListView
import com.mapzen.pelias.widget.PeliasSearchView
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
import com.squareup.okhttp.HttpResponseCache
import com.squareup.otto.Bus
import org.oscim.android.MapView
import org.oscim.layers.marker.MarkerSymbol
import org.oscim.map.Map
import org.oscim.tiling.source.OkHttpEngine
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.util.ArrayList
import javax.inject.Inject

public class MainActivity : AppCompatActivity(), ViewController,
        SearchResultsView.OnSearchResultSelectedListener, PoiLayer.OnPoiClickListener,
        Router.Callback {
    private val BASE_TILE_URL: String = "http://vector.dev.mapzen.com/osm/all"
    private val STYLE_PATH: String = "styles/mapzen.xml"
    private val FIND_ME_ICON: Int = android.R.drawable.star_big_on
    private val LOCATION_UPDATE_INTERVAL_IN_MS: Long = 1000L
    private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT: Float = 0f

    public val requestCodeSearchResults: Int = 0x01;

    var locationClient: LostApiClient? = null
    @Inject set
    var tileCache: HttpResponseCache? = null
    @Inject set
    var savedSearch: SavedSearch? = null
    @Inject set
    var presenter: MainPresenter? = null
    @Inject set
    var markerSymbolFactory: MarkerSymbolFactory? = null
    @Inject set
    var bus: Bus? = null
    @Inject set

    var app: PrivateMapsApplication? = null
    var mapController: MapController? = null
    var autoCompleteAdapter: AutoCompleteAdapter? = null
    var optionsMenu: Menu? = null
    var poiLayer: PoiLayer? = null
    var destination: Feature? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = getApplication() as PrivateMapsApplication
        app?.component()?.inject(this)
        presenter?.viewController = this
        presenter?.bus = bus;
        locationClient?.connect()
        initMapController()
        initPoiLayer()
        initAutoCompleteAdapter()
        initFindMeButton()
        centerOnCurrentLocation()
        presenter?.onRestoreViewState()
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
        bus?.unregister(presenter)
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
        val defaultMarker = markerSymbolFactory?.getDefaultMarker()
        val activeMarker = markerSymbolFactory?.getActiveMarker()
        if (map is Map && defaultMarker is MarkerSymbol && activeMarker is MarkerSymbol) {
            poiLayer = PoiLayer(map, defaultMarker, activeMarker)
            poiLayer?.onPoiClickListener = this
        }
    }

    private fun initAutoCompleteAdapter() {
        autoCompleteAdapter = AutoCompleteAdapter(this, R.layout.list_item_auto_complete)
        autoCompleteAdapter?.setRecentSearchIconResourceId(R.drawable.ic_recent)
        autoCompleteAdapter?.setAutoCompleteIconResourceId(R.drawable.ic_pin_outline)
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
            R.id.action_settings -> { onActionSettings(); return true }
            R.id.action_search -> { onActionSearch(); return true }
            R.id.action_clear -> { onActionClear(); return true }
            R.id.action_view_all -> { onActionViewAll(); return true }
        }

        return super<AppCompatActivity>.onOptionsItemSelected(item)
    }

    private fun onActionSettings() {
    }

    private fun onActionSearch() {
    }

    private fun onActionClear() {
        savedSearch?.clear()
        autoCompleteAdapter?.clear()
        autoCompleteAdapter?.notifyDataSetChanged()
    }

    private fun onActionViewAll() {
        presenter?.onViewAllSearchResults()
    }

    override fun showAllSearchResults(features: List<Feature>) {
        val simpleFeatures: ArrayList<SimpleFeature> = ArrayList()
        for (feature in features) {
            simpleFeatures.add(SimpleFeature.fromFeature(feature))
        }

        val menuItem = optionsMenu?.findItem(R.id.action_search)
        val actionView = menuItem?.getActionView() as PeliasSearchView
        val intent = Intent(this, javaClass<SearchResultsListActivity>())
        intent.putParcelableArrayListExtra("features", simpleFeatures)
        intent.putExtra("query", actionView.getQuery().toString())
        startActivityForResult(intent, requestCodeSearchResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode > 0) {
            (findViewById(R.id.search_results) as SearchResultsView).setCurrentItem(resultCode - 1)
        }
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
                showActionViewAll()
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
            hideProgress()
            Log.e(TAG, "Error fetching search results: " + error?.getMessage())
            Toast.makeText(this@MainActivity, "Error fetching search results",
                    Toast.LENGTH_LONG).show()
        }
    }

    inner class SearchOnActionExpandListener : MenuItemCompat.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
            presenter?.onExpandSearchView()
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
        pager.onSearchResultsSelectedListener = this
    }

    private fun addSearchResultsToMap(features: List<Feature>) {
        poiLayer?.removeAllItems()
        poiLayer?.addAll(features)
        centerOnCurrentFeature(features)
    }

    override fun centerOnCurrentFeature(features: List<Feature>) {
        Handler().postDelayed(Runnable {
            val pager = findViewById(R.id.search_results) as SearchResultsView
            val position = pager.getCurrentItem();
            val feature = SimpleFeature.fromFeature(features.get(position))
            val location = Location("map");
            location.setLatitude(feature.getLat())
            location.setLongitude(feature.getLon())
            poiLayer?.resetAllItems()
            poiLayer?.setActiveItem(position)
            mapController?.resetMapAndCenterOn(location)
            mapController?.getMap()?.updateMap(true)
        }, 100)
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

    override fun showProgress() = findViewById(R.id.progress).setVisibility(View.VISIBLE)

    override fun hideProgress() = findViewById(R.id.progress).setVisibility(View.GONE)

    override fun showOverflowMenu() = optionsMenu?.setGroupVisible(R.id.menu_overflow, true)

    override fun hideOverflowMenu() = optionsMenu?.setGroupVisible(R.id.menu_overflow, false)

    override fun onSearchResultSelected(position: Int) = presenter?.onSearchResultSelected(position)

    override fun onPoiClick(position: Int) =
            (findViewById(R.id.search_results) as SearchResultsView).setCurrentItem(position)

    override fun showActionViewAll() {
        optionsMenu?.findItem(R.id.action_view_all)?.setVisible(true)
    }

    override fun hideActionViewAll() {
        optionsMenu?.findItem(R.id.action_view_all)?.setVisible(false)
    }

    override fun collapseSearchView() {
        optionsMenu?.findItem(R.id.action_search)?.collapseActionView()
    }

    override fun showRoutePreview(feature: Feature) {
        this.destination = feature
        val simpleFeature = SimpleFeature.fromFeature(feature)
        val location = LocationServices.FusedLocationApi?.getLastLocation();
        if (location is Location) {
            val start: DoubleArray = doubleArrayOf(location.getLatitude(), location.getLongitude())
            val dest: DoubleArray = doubleArrayOf(simpleFeature.getLat(), simpleFeature.getLon())
            Router.getRouter().setLocation(start).setLocation(dest).setCallback(this).fetch()
        }
    }

    override fun success(route: Route?) {
        runOnUiThread({
            getSupportActionBar()?.hide()
            findViewById(R.id.route_preview).setVisibility(View.VISIBLE)
            (findViewById(R.id.route_preview) as RoutePreviewView).destination =
                    SimpleFeature.fromFeature(destination);
            (findViewById(R.id.route_preview) as RoutePreviewView).route = route;
        })
    }

    override fun failure(statusCode: Int) {
        Toast.makeText(this@MainActivity, "No route found", Toast.LENGTH_LONG).show()
    }

    override fun hideRoutePreview() {
        getSupportActionBar()?.show()
        findViewById(R.id.route_preview).setVisibility(View.GONE)
    }

    override fun onBackPressed() {
        presenter?.onBackPressed()
    }

    override fun shutDown() {
        finish()
    }
}
