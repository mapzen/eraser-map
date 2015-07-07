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
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.mapzen.android.lost.api.LocationRequest
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.android.lost.api.LostApiClient
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.erasermap.util.DouglasPeuckerReducer
import com.mapzen.pelias.Pelias
import com.mapzen.pelias.PeliasLocationProvider
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Result
import com.mapzen.pelias.widget.AutoCompleteAdapter
import com.mapzen.pelias.widget.AutoCompleteListView
import com.mapzen.pelias.widget.PeliasSearchView
import com.mapzen.tangram.MapController
import com.mapzen.tangram.MapView
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
import com.squareup.okhttp.Cache
import com.squareup.otto.Bus
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.util.ArrayList
import javax.inject.Inject

public class MainActivity : AppCompatActivity(), ViewController, Router.Callback,
        SearchResultsView.OnSearchResultSelectedListener {
    private val LOCATION_UPDATE_INTERVAL_IN_MS: Long = 1000L
    private val LOCATION_UPDATE_SMALLEST_DISPLACEMENT: Float = 0f

    public val requestCodeSearchResults: Int = 0x01

    private var route: Route? = null;
    var locationClient: LostApiClient? = null
    @Inject set
    var tileCache: Cache? = null
    @Inject set
    var savedSearch: SavedSearch? = null
    @Inject set
    var presenter: MainPresenter? = null
    @Inject set
    var bus: Bus? = null
    @Inject set

    var app: PrivateMapsApplication? = null
    var mapController : MapController? = null
    var autoCompleteAdapter: AutoCompleteAdapter? = null
    var optionsMenu: Menu? = null
    var destination: Feature? = null
    var type : Router.Type = Router.Type.DRIVING
    var reverse : Boolean = false;
    var currentLocation : Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super<AppCompatActivity>.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        app = getApplication() as PrivateMapsApplication
        app?.component()?.inject(this)
        presenter?.viewController = this
        presenter?.bus = bus
        locationClient?.connect()
        initMapController()
        initAutoCompleteAdapter()
        initFindMeButton()
        initReverseButton()
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
                .commit()
    }

    override fun onDestroy() {
        super<AppCompatActivity>.onDestroy()
        saveCurrentSearchTerm()
        bus?.unregister(presenter)
    }

    private fun initMapController() {
        val mapView = findViewById(R.id.map) as MapView
        mapController = MapController(this, mapView)
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
                .setFastestInterval(LOCATION_UPDATE_INTERVAL_IN_MS)
                .setSmallestDisplacement(LOCATION_UPDATE_SMALLEST_DISPLACEMENT)

        LocationServices.FusedLocationApi?.requestLocationUpdates(locationRequest) {
            location: Location -> currentLocation = location
            val routeModeView = findViewById(R.id.route_mode) as RouteModeView
            routeModeView.onLocationChanged(location)
        }
    }

    private fun centerOnCurrentLocation() {
        val location = LocationServices.FusedLocationApi?.getLastLocation()
        if (location != null) {
            currentLocation = location
            mapController?.setMapPosition(location.getLongitude(), location.getLatitude())
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
            val pelias = Pelias.getPelias()
            pelias.setLocationProvider(LocationProvider())
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
            R.id.action_settings -> {
                onActionSettings(); return true
            }
            R.id.action_search -> {
                onActionSearch(); return true
            }
            R.id.action_clear -> {
                onActionClear(); return true
            }
            R.id.action_view_all -> {
                onActionViewAll(); return true
            }
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
        private val TAG: String = "PeliasCallback"

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

    inner class LocationProvider() : PeliasLocationProvider {
        override fun getLat(): String? {
            return currentLocation?.getLatitude().toString()
        }

        override fun getLon(): String? {
            return currentLocation?.getLongitude().toString()
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
        centerOnCurrentFeature(features)
    }

    override fun centerOnCurrentFeature(features: List<Feature>) {
        Handler().postDelayed(Runnable {
            val pager = findViewById(R.id.search_results) as SearchResultsView
            val position = pager.getCurrentItem()
            val feature = SimpleFeature.fromFeature(features.get(position))
            val location = Location("map")
            location.setLatitude(feature.getLat())
            location.setLongitude(feature.getLon())
        }, 100)
    }

    override fun hideSearchResults() {
        hideSearchResultsPager()
    }

    private fun hideSearchResultsPager() {
        (findViewById(R.id.search_results) as SearchResultsView).setVisibility(View.GONE)
    }

    override fun showProgress() = findViewById(R.id.progress).setVisibility(View.VISIBLE)

    override fun hideProgress() = findViewById(R.id.progress).setVisibility(View.GONE)

    override fun showOverflowMenu() = optionsMenu?.setGroupVisible(R.id.menu_overflow, true)

    override fun hideOverflowMenu() = optionsMenu?.setGroupVisible(R.id.menu_overflow, false)

    override fun onSearchResultSelected(position: Int) = presenter?.onSearchResultSelected(position)

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
        route()
    }

    override fun success(route: Route?) {
        this.route = route;
        runOnUiThread({
            if( findViewById(R.id.route_mode).getVisibility() != View.VISIBLE) {
                getSupportActionBar()?.hide()
                findViewById(R.id.route_preview).setVisibility(View.VISIBLE)
                (findViewById(R.id.route_preview) as RoutePreviewView).destination =
                        SimpleFeature.fromFeature(destination);
                (findViewById(R.id.route_preview) as RoutePreviewView).route = route;
            }
        })
        updateRoutePreview()
    }

    override fun failure(statusCode: Int) {
        Toast.makeText(this@MainActivity, "No route found", Toast.LENGTH_LONG).show()
    }

    override fun hideRoutePreview() {
        getSupportActionBar()?.show()
        reverse = false
        findViewById(R.id.route_preview).setVisibility(View.GONE)
    }

    fun route() {
        val simpleFeature = SimpleFeature.fromFeature(destination)
        val location = LocationServices.FusedLocationApi?.getLastLocation()
        if (reverse) {
            if (location is Location) {
                val start: DoubleArray = doubleArrayOf(simpleFeature.getLat(), simpleFeature.getLon())
                val dest: DoubleArray = doubleArrayOf(location.getLatitude(), location.getLongitude())
                getInitializedRouter().setLocation(start).setLocation(dest).setCallback(this).fetch()
            }
        } else {
            if (location is Location) {
                val start: DoubleArray = doubleArrayOf(location.getLatitude(), location.getLongitude())
                val dest: DoubleArray = doubleArrayOf(simpleFeature.getLat(), simpleFeature.getLon())
                getInitializedRouter().setLocation(start).setLocation(dest).setCallback(this).fetch()
            }
        }
    }

    fun updateRoutePreview() {
        (findViewById(R.id.by_car) as RadioButton).setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                type = Router.Type.DRIVING
                route()
                (findViewById(R.id.routing_circle) as ImageButton)
                        .setImageResource(R.drawable.ic_start_car_normal)
            }
        }
        (findViewById(R.id.by_foot) as RadioButton).setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                type = Router.Type.WALKING
                route()
                (findViewById(R.id.routing_circle) as ImageButton)
                        .setImageResource(R.drawable.ic_start_walk_normal)
            }
        }
        (findViewById(R.id.by_bike) as RadioButton).setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                type = Router.Type.BIKING
                route()
                (findViewById(R.id.routing_circle) as ImageButton)
                        .setImageResource(R.drawable.ic_start_bike_normal)
            }
        }
    }

    private fun reverse() {
        reverse = !reverse;
        (findViewById(R.id.route_preview) as RoutePreviewView).reverse = this.reverse
        if(reverse) {
            findViewById(R.id.starting_location_icon).setVisibility(View.GONE)
            findViewById(R.id.destination_location_icon).setVisibility(View.VISIBLE)
        } else {
            findViewById(R.id.starting_location_icon).setVisibility(View.VISIBLE)
            findViewById(R.id.destination_location_icon).setVisibility(View.GONE)
        }
        route()
    }

    private fun initReverseButton() {
        (findViewById(R.id.route_reverse) as ImageButton).setOnClickListener({ reverse()})

        (findViewById(R.id.routing_circle) as ImageButton).setOnClickListener (
                {presenter?.onRoutingCircleClick(reverse)})
    }

    override fun onBackPressed() {
        presenter?.onBackPressed()
        centerOnCurrentLocation()
    }

    override fun shutDown() {
        finish()
    }

    override fun showDirectionList() {
        val instructionStrings = ArrayList<String>()
        val instructionType= ArrayList<Int>()
        val instructionDistance= ArrayList<Int>()
        for(instruction in route!!.getRouteInstructions() ) {
            instructionStrings.add(instruction.getHumanTurnInstruction())
            instructionType.add(instruction.turnInstruction)
            instructionDistance.add(instruction.distance)
        }
        val simpleFeature = SimpleFeature.fromFeature(destination)
        val intent = Intent(this, javaClass<InstructionListActivity>())
        intent.putExtra("instruction_strings", instructionStrings)
        intent.putExtra("instruction_types", instructionType)
        intent.putExtra("instruction_distances", instructionDistance)
        intent.putExtra("destination", simpleFeature.toString())
        intent.putExtra("reverse", this.reverse)
        startActivityForResult(intent, requestCodeSearchResults)
    }

    override fun showRoutingMode() {
        reverse = false
        findViewById(R.id.route_preview).setVisibility(View.GONE)
        findViewById(R.id.route_mode).setVisibility(View.VISIBLE)
        route()

        val pager = findViewById(R.id.route_mode) as RouteModeView
        val adapter = InstructionAdapter(this, route!!.getRouteInstructions(), pager)
        val simpleFeature = SimpleFeature.fromFeature(destination)
        pager.route = this.route
        pager.routeEngine?.setRoute(route)
        pager.setAdapter(adapter)
        pager.setVisibility(View.VISIBLE)
        (findViewById(R.id.destination_name) as TextView).setText(simpleFeature.toString())
    }

    override fun hideRoutingMode() {
        findViewById(R.id.route_mode).setVisibility(View.GONE)
        findViewById(R.id.route_preview).setVisibility(View.VISIBLE)
        getSupportActionBar()?.hide()
        val routeModeView = findViewById(R.id.route_mode) as RouteModeView
        routeModeView.route = null
    }

    private fun getInitializedRouter(): Router {
        when(type) {
            Router.Type.DRIVING -> return Router().setApiKey(BuildConfig.VALHALLA_API_KEY).setDriving()
            Router.Type.WALKING -> return Router().setApiKey(BuildConfig.VALHALLA_API_KEY).setWalking()
            Router.Type.BIKING -> return Router().setApiKey(BuildConfig.VALHALLA_API_KEY).setBiking()
        }
    }
}
