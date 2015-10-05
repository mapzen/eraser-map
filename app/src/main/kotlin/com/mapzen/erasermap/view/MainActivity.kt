package com.mapzen.erasermap.view

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.CrashReportService
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.model.RouterFactory
import com.mapzen.erasermap.model.TileHttpHandler
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.leyndo.ManifestDownLoader
import com.mapzen.leyndo.ManifestModel
import com.mapzen.pelias.Pelias
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Geometry
import com.mapzen.pelias.gson.Properties
import com.mapzen.pelias.gson.Result
import com.mapzen.pelias.widget.AutoCompleteAdapter
import com.mapzen.pelias.widget.AutoCompleteListView
import com.mapzen.pelias.widget.PeliasSearchView
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.MapController
import com.mapzen.tangram.MapData
import com.mapzen.tangram.MapView
import com.mapzen.valhalla.Instruction
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import com.mapzen.valhalla.Router.DistanceUnits
import com.squareup.otto.Bus
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.util.ArrayList
import javax.inject.Inject

public class MainActivity : AppCompatActivity(), MainViewController, RouteCallback,
        SearchResultsView.OnSearchResultSelectedListener {

    public val requestCodeSearchResults: Int = 0x01

    private var route: Route? = null;

    var savedSearch: SavedSearch? = null
        @Inject set
    var presenter: MainPresenter? = null
        @Inject set
    var bus: Bus? = null
        @Inject set
    var crashReportService: CrashReportService? = null
        @Inject set
    var routerFactory: RouterFactory? = null
        @Inject set
    var settings: AppSettings? = null
        @Inject set
    var tileHttpHandler: TileHttpHandler? = null
        @Inject set

    var apiKeys: ManifestModel? = null
    var app: EraserMapApplication? = null
    var mapController : MapController? = null
    var autoCompleteAdapter: AutoCompleteAdapter? = null
    var optionsMenu: Menu? = null
    var origin: Location? = null
    var destination: Feature? = null
    var type: Router.Type = Router.Type.DRIVING
    var reverse: Boolean = false
    var routeLine: MapData? = null
    var findMe: MapData? = null
    var searchResults: MapData? = null
    var manifestRequestCount: Int = 0

    override public fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as EraserMapApplication
        app?.component()?.inject(this)
        initCrashReportService()
        setContentView(R.layout.activity_main)
        presenter?.mainViewController = this
        presenter?.bus = bus
        initMapController()
        initAutoCompleteAdapter()
        initFindMeButton()
        initReverseButton()
        presenter?.onCreate()
        presenter?.onRestoreViewState()
        getApiKeys()
    }

    override public fun onStart() {
        super.onStart()
        savedSearch?.deserialize(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SavedSearch.TAG, null))
    }

    override public fun onResume() {
        super.onResume()
        presenter?.onResume()
    }

    override public fun onPause() {
        super.onPause()
        presenter?.onPause()
    }

    override public fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(SavedSearch.TAG, savedSearch?.serialize())
                .commit()
    }

    override public fun onDestroy() {
        super.onDestroy()
        saveCurrentSearchTerm()
        bus?.unregister(presenter)
    }

    private fun initMapController() {
        val mapView = findViewById(R.id.map) as MapView
        mapController = MapController(this, mapView, "eraser-map.yaml")
        mapController?.setLongPressListener(View.OnGenericMotionListener {
            view, motionEvent -> reverseGeolocate(motionEvent) })
        mapController?.setHttpHandler(tileHttpHandler)
    }

    private fun initAutoCompleteAdapter() {
        autoCompleteAdapter = AutoCompleteAdapter(this, R.layout.list_item_auto_complete)
        autoCompleteAdapter?.setRecentSearchIconResourceId(R.drawable.ic_recent)
        autoCompleteAdapter?.setAutoCompleteIconResourceId(R.drawable.ic_pin_outline)
    }

    private fun initFindMeButton() {
        findMe = MapData("find_me")
        findViewById(R.id.find_me).setOnClickListener({ presenter?.onFindMeButtonClick() })
    }

    private fun initCrashReportService() {
        crashReportService?.initAndStartSession(this)
    }

    private fun getApiKeys() {
        manifestRequestCount++;
        apiKeys = ManifestModel()
        try {
            var dl: ManifestDownLoader = ManifestDownLoader()
            apiKeys = dl.getManifestModel( {
                checkForNullKeys()
            })
        } catch (e: UnsatisfiedLinkError) {
            checkForNullKeys()
            if ("Dalvik".equals(System.getProperty("java.vm.name"))) {
                throw e;
            }
        }
    }

    private fun checkForNullKeys() {
        if(apiKeys?.valhallaApiKey.isNullOrEmpty() && manifestRequestCount < 2) {
            getApiKeys()
        } else if(apiKeys?.valhallaApiKey.isNullOrEmpty()
                .and(BuildConfig.VALHALLA_API_KEY.isNullOrEmpty())) {
            var builder: AlertDialog.Builder = AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.manifest_error))
                    .setNegativeButton(getString(R.string.decline_update),
                            DialogInterface.OnClickListener { dialogInterface, i -> finish() })
                    .setCancelable(false)
            builder.create().show()
        } else {
            if (apiKeys?.valhallaApiKey == null) {
                apiKeys?.valhallaApiKey = BuildConfig.VALHALLA_API_KEY
            }
            if (apiKeys?.vectorTileApiKeyReleaseProp == null) {
                apiKeys?.vectorTileApiKeyReleaseProp = BuildConfig.VECTOR_TILE_API_KEY
            }
            if (apiKeys?.minVersion != null) {
                checkIfUpdateNeeded()
            }
            routerFactory?.apiKey = apiKeys?.valhallaApiKey
            tileHttpHandler?.apiKey = apiKeys?.vectorTileApiKeyReleaseProp
        }
    }

    public fun checkIfUpdateNeeded() {
        if(apiKeys?.minVersion as Int > BuildConfig.VERSION_CODE ) {
            var builder: AlertDialog.Builder  = AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.update_message))
                    .setPositiveButton(getString(R.string.accept_update),
                            DialogInterface.OnClickListener { dialogInterface, i ->
                            startActivity(Intent(Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=" + packageName)))
                            finish()
                         })
                    .setNegativeButton(getString(R.string.decline_update),
                            DialogInterface.OnClickListener { dialogInterface, i -> finish() })
                    .setCancelable(false)
            builder.create().show()
       }
    }

    override fun centerMapOnLocation(location: Location, zoom: Float) {
        mapController?.setMapPosition(location.longitude, location.latitude)
        mapController?.mapZoom = zoom
        showCurrentLocation(location)
    }

    override fun showCurrentLocation(location: Location) {
        findMe?.clear()
        findMe?.addPoint(LngLat(location.longitude, location.latitude))
    }

    override fun setMapTilt(radians: Float) {
        mapController?.mapTilt = radians
    }

    override fun setMapRotation(radians: Float) {
        mapController?.mapRotation = radians
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        optionsMenu = menu

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search),
                SearchOnActionExpandListener())

        val searchView = menu.findItem(R.id.action_search).actionView
        val listView = findViewById(R.id.auto_complete) as AutoCompleteListView
        val emptyView = findViewById(android.R.id.empty)

        if (searchView is PeliasSearchView) {
            listView.adapter = autoCompleteAdapter
            val pelias = Pelias.getPelias()
            pelias.setLocationProvider(presenter?.getPeliasLocationProvider())
            pelias.apiKey = apiKeys?.peliasApiKey
            searchView.setAutoCompleteListView(listView)
            searchView.setSavedSearch(savedSearch)
            searchView.setPelias(Pelias.getPelias())
            searchView.setCallback(PeliasCallback())
            searchView.setOnSubmitListener({ presenter?.onQuerySubmit() })
            listView.emptyView = emptyView
            restoreCurrentSearchTerm()
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_settings -> { onActionSettings(); return true }
            R.id.action_search -> { onActionSearch(); return true }
            R.id.action_clear -> { onActionClear(); return true }
            R.id.action_view_all -> { onActionViewAll(); return true }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onActionSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
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
        val actionView = menuItem?.actionView as PeliasSearchView
        setBoundingBox()
        val intent = Intent(this, SearchResultsListActivity::class.java)
        intent.putParcelableArrayListExtra("features", simpleFeatures)
        intent.putExtra("query", actionView.query.toString())
        startActivityForResult(intent, requestCodeSearchResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode > 0) {
            (findViewById(R.id.search_results) as SearchResultsView).setCurrentItem(resultCode - 1)
        }
    }

    private fun saveCurrentSearchTerm() {
        val menuItem = optionsMenu?.findItem(R.id.action_search)
        val actionView = menuItem?.actionView
        val isExpanded = menuItem?.isActionViewExpanded ?: false
        if (actionView is PeliasSearchView && isExpanded) {
            presenter?.currentSearchTerm = actionView.query.toString()
        }
    }

    private fun restoreCurrentSearchTerm() {
        val menuItem = optionsMenu?.findItem(R.id.action_search)
        val actionView = menuItem?.actionView as PeliasSearchView
        val term = presenter?.currentSearchTerm
        if (term != null) {
            menuItem?.expandActionView()
            actionView.setQuery(term, false)
            if (findViewById(R.id.search_results).visibility == View.VISIBLE) {
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

    inner class ReversePeliasCallback : Callback<Result> {
        private val TAG: String = "ReversePeliasCallback"

        override fun success(result: Result?, response: Response?) {
            presenter?.onReverseGeocodeResultsAvailable(result)
        }

        override fun failure(error: RetrofitError?) {
            hideProgress()
            Log.e(TAG, "Error Reverse Geolocating: " + error?.getMessage())
        }
    }

    inner class SearchOnActionExpandListener : MenuItemCompat.OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem?): Boolean {
            setBoundingBox()
            presenter?.onExpandSearchView()
            return true
        }

        override fun onMenuItemActionCollapse(item: MenuItem?): Boolean {
            presenter?.onCollapseSearchView()
            return true
        }
    }

    override fun showSearchResults(features: List<Feature>) {
        showSearchResultsPager(features)
        addSearchResultsToMap(features)
    }

    private fun showSearchResultsPager(features: List<Feature>) {
        val pager = findViewById(R.id.search_results) as SearchResultsView
        pager.setAdapter(SearchResultsAdapter(this, features))
        pager.visibility = View.VISIBLE
        pager.onSearchResultsSelectedListener = this
    }

    override fun showReverseGeocodeFeature(features: List<Feature>) {
        val pager = findViewById(R.id.search_results) as SearchResultsView
        pager.setAdapter(SearchResultsAdapter(this, features.subList(0, 1)))
        pager.visibility = View.VISIBLE
        pager.onSearchResultsSelectedListener = this
    }

    private fun addSearchResultsToMap(features: List<Feature>) {
        centerOnCurrentFeature(features)

        if (searchResults == null) {
            searchResults = MapData("search")
        }

        searchResults?.clear()
        for (feature in features) {
            val simpleFeature = SimpleFeature.fromFeature(feature)
            val lngLat = LngLat(simpleFeature.lon, simpleFeature.lat)
            searchResults?.addPoint(lngLat)
        }
    }

    override fun centerOnCurrentFeature(features: List<Feature>) {
        Handler().postDelayed({
            val pager = findViewById(R.id.search_results) as SearchResultsView
            val position = pager.getCurrentItem()
            val feature = SimpleFeature.fromFeature(features.get(position))
            mapController?.setMapPosition(feature.lon,feature.lat)
            mapController?.mapZoom = MainPresenter.DEFAULT_ZOOM
        }, 100)
    }

    public fun reverseGeolocate(event: MotionEvent) : Boolean {
        val pelias = Pelias.getPelias()
        pelias.setLocationProvider(presenter?.getPeliasLocationProvider())
        var coords  = mapController?.coordinatesAtScreenPosition(
                event.rawX.toDouble(), event.rawY.toDouble())
        presenter?.currentFeature = getGenericLocationFeature(coords?.latitude as Double,
                coords?.longitude as Double)
        pelias.reverse(coords?.latitude.toString(), coords?.longitude.toString(),
                ReversePeliasCallback())
        return true
    }

    override fun hideSearchResults() {
        hideSearchResultsPager()
        searchResults?.clear()
    }

    private fun hideSearchResultsPager() {
        (findViewById(R.id.search_results) as SearchResultsView).visibility = View.GONE
    }

    override fun showProgress() {
        findViewById(R.id.progress).visibility = View.VISIBLE
    }

    override fun hideProgress() {
        findViewById(R.id.progress).visibility = View.GONE
    }

    override fun showOverflowMenu() {
        optionsMenu?.setGroupVisible(R.id.menu_overflow, true)
    }

    override fun hideOverflowMenu() {
        optionsMenu?.setGroupVisible(R.id.menu_overflow, false)
    }

    override fun onSearchResultSelected(position: Int) {
        presenter?.onSearchResultSelected(position)
    }

    override fun showActionViewAll() {
        optionsMenu?.findItem(R.id.action_view_all)?.setVisible(true)
    }

    override fun hideActionViewAll() {
        optionsMenu?.findItem(R.id.action_view_all)?.setVisible(false)
    }

    override fun collapseSearchView() {
        optionsMenu?.findItem(R.id.action_search)?.collapseActionView()
    }

    override fun showRoutePreview(location: Location, feature: Feature) {
        this.origin = location
        this.destination = feature
        route()
        (findViewById(R.id.route_preview) as RoutePreviewView).destination =
                SimpleFeature.fromFeature(destination);
        (findViewById(R.id.route_preview) as RoutePreviewView).route = route
    }

    override fun clearRouteLine() {
        routeLine?.clear()
    }

    override fun success(route: Route) {
        this.route = route;
        presenter?.route = route;
        runOnUiThread   ({
            if( findViewById(R.id.route_mode).visibility != View.VISIBLE) {
                supportActionBar?.hide()
                findViewById(R.id.route_preview).visibility = View.VISIBLE
            }
        })
        updateRoutePreview()
        drawRouteLine(route)
    }

    private fun drawRouteLine(route: Route) {
        val geometry: ArrayList<Location>? = route.getGeometry()
        val mapGeometry: ArrayList<LngLat> = ArrayList()
        if (geometry is ArrayList<Location>) {
            for (location in geometry) {
                mapGeometry.add(LngLat(location.longitude, location.latitude))
            }
        }

        if (routeLine == null) {
            routeLine = MapData("route")
        }

        routeLine?.clear()
        routeLine?.addLine(mapGeometry)
    }

    override fun failure(statusCode: Int) {
        Toast.makeText(this@MainActivity, "No route found", Toast.LENGTH_LONG).show()
    }

    override fun hideRoutePreview() {
        if((findViewById(R.id.route_mode) as RouteModeView).visibility != View.VISIBLE) {
            supportActionBar?.show()
            reverse = false
            findViewById(R.id.route_preview).visibility = View.GONE
        }
    }

    private fun route() {
        val simpleFeature = SimpleFeature.fromFeature(destination)
        val location = origin
        if (reverse) {
            if (location is Location) {
                val start: DoubleArray = doubleArrayOf(simpleFeature.lat, simpleFeature.lon)
                val dest: DoubleArray = doubleArrayOf(location.latitude, location.longitude)
                val units: DistanceUnits = settings?.distanceUnits ?: DistanceUnits.MILES
                routerFactory?.getInitializedRouter(type)
                        ?.setLocation(start)
                        ?.setLocation(dest)
                        ?.setDistanceUnits(units)
                        ?.setCallback(this)
                        ?.fetch()
            }
        } else {
            if (location is Location) {
                val start: DoubleArray = doubleArrayOf(location.latitude, location.longitude)
                val dest: DoubleArray = doubleArrayOf(simpleFeature.lat, simpleFeature.lon)
                val units: DistanceUnits = settings?.distanceUnits ?: DistanceUnits.MILES
                val name = destination?.properties?.name
                val street = simpleFeature.title
                val city = simpleFeature.city
                val state = simpleFeature.admin
                routerFactory?.getInitializedRouter(Router.Type.DRIVING)
                        ?.setLocation(start)
                        ?.setLocation(dest, name, street, city, state)
                        ?.setDistanceUnits(units)
                        ?.setCallback(this)
                        ?.fetch()
            }
        }
    }

    private fun updateRoutePreview() {
        (findViewById(R.id.by_car) as RadioButton)
                .setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                type = Router.Type.DRIVING
                route()
                (findViewById(R.id.routing_circle) as ImageButton)
                        .setImageResource(R.drawable.ic_start_car_normal)
            }
        }

        (findViewById(R.id.by_foot) as RadioButton)
                .setOnCheckedChangeListener { compoundButton, b ->
            if (b) {
                type = Router.Type.WALKING
                route()
                (findViewById(R.id.routing_circle) as ImageButton)
                        .setImageResource(R.drawable.ic_start_walk_normal)
            }
        }

        (findViewById(R.id.by_bike) as RadioButton)
                .setOnCheckedChangeListener { compoundButton, b ->
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
            findViewById(R.id.starting_location_icon).visibility = View.GONE
            findViewById(R.id.destination_location_icon).visibility = View.VISIBLE
        } else {
            findViewById(R.id.starting_location_icon).visibility = View.VISIBLE
            findViewById(R.id.destination_location_icon).visibility = View.GONE
        }
        route()
    }

    private fun initReverseButton() {
        (findViewById(R.id.route_reverse) as ImageButton).setOnClickListener({ reverse() })
        (findViewById(R.id.routing_circle) as ImageButton).setOnClickListener ({
            presenter?.onRoutingCircleClick(reverse)
        })
    }

    override fun onBackPressed() {
        presenter?.onBackPressed()
    }

    override fun shutDown() {
        finish()
    }

    override fun showDirectionList() {
        val instructionStrings = ArrayList<String>()
        val instructionType = ArrayList<Int>()
        val instructionDistance = ArrayList<Int>()
        val instructions = route?.getRouteInstructions()
        if (instructions != null) {
            for(instruction in instructions) {
                val humanInstruction = instruction.getHumanTurnInstruction()
                if (humanInstruction is String) {
                    instructionStrings.add(humanInstruction)
                }
                instructionType.add(instruction.turnInstruction)
                instructionDistance.add(instruction.distance)
            }
        }

        val simpleFeature = SimpleFeature.fromFeature(destination)
        val intent = Intent(this, InstructionListActivity::class.java)
        intent.putExtra("instruction_strings", instructionStrings)
        intent.putExtra("instruction_types", instructionType)
        intent.putExtra("instruction_distances", instructionDistance)
        intent.putExtra("destination", simpleFeature.toString())
        intent.putExtra("reverse", this.reverse)
        startActivityForResult(intent, requestCodeSearchResults)
    }

    override fun showRoutingMode(feature: Feature) {
        findMe?.clear()
        findMe = null
        val startingLocation = route?.getRouteInstructions()?.get(0)?.location
        if (startingLocation is Location) {
            centerMapOnLocation(startingLocation, MainPresenter.ROUTING_ZOOM)
        }

        findViewById(R.id.find_me).visibility = View.GONE
        supportActionBar?.hide()
        presenter?.routingEnabled = true
        this.destination = feature
        reverse = false
        findViewById(R.id.route_preview).visibility = View.GONE
        findViewById(R.id.route_mode).visibility = View.VISIBLE
        (findViewById(R.id.route_mode) as RouteModeView).mainPresenter = presenter
        (findViewById(R.id.route_mode) as RouteModeView).mapController = mapController
        presenter?.routeViewController = findViewById(R.id.route_mode) as RouteModeView
        this.route = presenter?.route
        val pager = findViewById(R.id.route_mode) as RouteModeView
        pager.route = this.route
        pager.routePresenter?.setRoute(route)
        pager.voiceNavigationController = VoiceNavigationController(this)

        val instructions = route?.getRouteInstructions()
        if (instructions != null) {
            val adapter = InstructionAdapter(this, instructions, pager)
            pager.setAdapter(adapter)
            pager.visibility = View.VISIBLE
        }

        val firstInstruction = route?.getRouteInstructions()?.get(0)
        if (firstInstruction is Instruction) {
            presenter?.onInstructionSelected(firstInstruction)
        }

        val simpleFeature = SimpleFeature.fromFeature(destination)
        (findViewById(R.id.destination_name) as TextView).text = simpleFeature.toString()
    }

    override fun hideRoutingMode() {
        initFindMeButton()
        presenter?.routingEnabled = false
        val routeModeView = findViewById(R.id.route_mode) as RouteModeView
        findViewById(R.id.route_mode).visibility = View.GONE
        findViewById(R.id.find_me).visibility = View.VISIBLE
        if (origin is Location && destination is Feature) {
            showRoutePreview(origin as Location, destination as Feature)
        }
        supportActionBar?.hide()
        routeModeView.route = null
        routeModeView.hideRouteIcon()
    }

    private fun setBoundingBox() {
        val menuItem = optionsMenu?.findItem(R.id.action_search)
        var mdisp : Display = getWindowManager().getDefaultDisplay();
        var minLatLon = mapController?.coordinatesAtScreenPosition(mdisp.width.toDouble(), mdisp.height.toDouble())
        var maxLatLon = mapController?.coordinatesAtScreenPosition(0.0, 0.0);
        (menuItem?.actionView  as PeliasSearchView).setBoundingBox(minLatLon?.latitude.toString(), minLatLon?.longitude.toString(),
                maxLatLon?.latitude.toString(), maxLatLon?.longitude.toString())
    }

    private fun getGenericLocationFeature(lat: Double, lon: Double) : Feature {
        var nameLength: Int = 6;
        val feature = Feature()
        val properties = Properties()
        if(lat.toString().length() > nameLength && lon.toString().length() > nameLength + 1) {
            properties.label = lat.toString().substring(0, nameLength) + "," + lon.toString()
                    .substring(0, nameLength + 1)
        }
        feature.properties = properties
        val geometry = Geometry()
        val coordinates = ArrayList<Double>()
        coordinates.add(lon)
        coordinates.add(lat)
        geometry.coordinates = coordinates
        feature.geometry = geometry
        return feature
    }
}
