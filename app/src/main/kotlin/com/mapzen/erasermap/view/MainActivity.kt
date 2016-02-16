package com.mapzen.erasermap.view

import android.content.Intent
import android.graphics.Point
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.support.v7.app.ActionBar
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.CrashReportService
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.model.RouteManager
import com.mapzen.erasermap.model.TileHttpHandler
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.erasermap.util.AxisAlignedBoundingBox
import com.mapzen.erasermap.util.AxisAlignedBoundingBox.PointD
import com.mapzen.erasermap.util.NotificationBroadcastReceiver
import com.mapzen.erasermap.util.NotificationCreator
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
import com.mapzen.tangram.Tangram
import com.mapzen.tangram.TouchInput
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.util.ArrayList
import javax.inject.Inject

public class MainActivity : AppCompatActivity(), MainViewController, RouteCallback,
        SearchResultsView.OnSearchResultSelectedListener {

    companion object {
        @JvmStatic val MAP_DATA_PROP_SEARCHINDEX = "searchIndex"
        @JvmStatic val MAP_DATA_PROP_STATE = "state"
        @JvmStatic val MAP_DATA_PROP_STATE_ACTIVE = "active"
        @JvmStatic val MAP_DATA_PROP_STATE_INACTIVE = "inactive"
        @JvmStatic val MAP_DATA_PROP_ID = "id"
        @JvmStatic val MAP_DATA_PROP_NAME = "name"
    }

    public val requestCodeSearchResults: Int = 0x01

    var savedSearch: SavedSearch? = null
        @Inject set
    var presenter: MainPresenter? = null
        @Inject set
    var crashReportService: CrashReportService? = null
        @Inject set
    var routeManager: RouteManager? = null
        @Inject set
    var settings: AppSettings? = null
        @Inject set
    var tileHttpHandler: TileHttpHandler? = null
        @Inject set
    var mapzenLocation: MapzenLocation? = null
        @Inject set

    var app: EraserMapApplication? = null
    var mapController : MapController? = null
    var autoCompleteAdapter: AutoCompleteAdapter? = null
    var optionsMenu: Menu? = null
    var findMe: MapData? = null
    var searchResults: MapData? = null
    var reverseGeocodeData: MapData? = null
    var startPin: MapData? = null
    var endPin: MapData? = null
    var poiTapPoint: FloatArray? = null
    var poiTapName: String? = null
    var searchView: PeliasSearchView? = null

    val findMeButton: ImageButton by lazy { findViewById(R.id.find_me) as ImageButton }
    val routePreviewView: RoutePreviewView by lazy { findViewById(R.id.route_preview) as RoutePreviewView }
    val routeModeView: RouteModeView by lazy { findViewById(R.id.route_mode) as RouteModeView }
    val reverseButton: ImageButton by lazy { findViewById(R.id.route_reverse) as ImageButton }
    val viewListButton: Button by lazy { findViewById(R.id.view_list) as Button }
    val startNavigationButton: Button by lazy { findViewById(R.id.start_navigation) as Button }
    val byCar: RadioButton by lazy { findViewById(R.id.by_car) as RadioButton }
    val byBike: RadioButton by lazy { findViewById(R.id.by_bike) as RadioButton }
    val byFoot: RadioButton by lazy { findViewById(R.id.by_foot) as RadioButton }
    val compass: CompassView by lazy { findViewById(R.id.compass_view) as CompassView }
    val routePreviewCompass: CompassView by lazy { findViewById(R.id.route_preview_compass_view) as CompassView }
    val routeModeCompass: CompassView by lazy { findViewById(R.id.route_mode_compass_view) as CompassView }
    val muteView: MuteView by lazy { findViewById(R.id.route_mode_mute_view) as MuteView }
    val searchResultsView: SearchResultsView by lazy { findViewById(R.id.search_results) as SearchResultsView }
    val osmAttributionText: TextView by lazy { findViewById(R.id.osm_attribution) as TextView }

    override public fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as EraserMapApplication
        app?.component()?.inject(this)
        initCrashReportService()
        setContentView(R.layout.activity_main)
        presenter?.mainViewController = this
        initMapController()
        initAutoCompleteAdapter()
        initFindMeButton()
        initMute()
        initCompass()
        initReverseButton()
        initMapRotateListener()
        presenter?.onCreate()
        presenter?.onRestoreViewState()
        supportActionBar?.setDisplayShowTitleEnabled(false)
        settings?.initTangramDebugFlags()
        routeModeView.voiceNavigationController = VoiceNavigationController(this)
    }

    override protected fun onNewIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(NotificationCreator.EXIT_NAVIGATION, false) as Boolean) {
            exitNavigation()
            if((intent?.getBooleanExtra(NotificationBroadcastReceiver.VISIBILITY, false)
                    as Boolean).not()) {
                moveTaskToBack(true)
            }
        }
    }

    private fun initMapRotateListener() {
        mapController?.setRotateResponder(TouchInput.RotateResponder {
            x, y, rotation -> presenter?.onMapMotionEvent() ?: false
        })
    }

    override fun rotateCompass() {
        val radians: Float = mapController?.mapRotation ?: 0f
        val degrees = Math.toDegrees(radians.toDouble()).toFloat()
        compass.rotation = degrees
        routePreviewCompass.rotation = degrees
        routeModeCompass.rotation = degrees
    }

    override public fun onStart() {
        super.onStart()
        savedSearch?.deserialize(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SavedSearch.TAG, null))
    }

    override public fun onResume() {
        super.onResume()
        presenter?.onResume()
        app?.onActivityResume()
        autoCompleteAdapter?.clear()
        autoCompleteAdapter?.notifyDataSetChanged()
    }

    override public fun onPause() {
        super.onPause()
        presenter?.onPause()
        app?.onActivityPause()
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
        routeModeView.clearRoute()
        findMe?.clear()
    }

    private fun initMapController() {
        val mapView = findViewById(R.id.map) as MapView
        mapController = MapController(this, mapView, "style/eraser-map.yaml")
        mapController?.setLongPressResponder({
            x, y -> presenter?.onReverseGeoRequested(x, y)
        })
        mapController?.setTapResponder(object: TouchInput.TapResponder {
            override fun onSingleTapUp(x: Float, y: Float): Boolean = false
            override fun onSingleTapConfirmed(x: Float, y: Float): Boolean {
                poiTapPoint = floatArrayOf(x, y)
                mapController?.pickFeature(x, y)
                return true
            }
        })
        mapController?.setDoubleTapResponder({ x, y ->
            val tappedPos = mapController?.coordinatesAtScreenPosition(x.toDouble(), y.toDouble())
            val currentPos = mapController?.mapPosition
            if (tappedPos != null && currentPos != null) {
                mapController?.setMapZoom((mapController?.mapZoom as Float) + 1.0f, 0.5f)
                mapController?.setMapPosition(
                        0.5f * (tappedPos.longitude + currentPos.longitude),
                        0.5f * (tappedPos.latitude + currentPos.latitude),
                        0.5f);
            }
            true;
        })
        mapController?.setFeatureTouchListener({
            properties, positionX, positionY ->
                // Reassign tapPoint to center of the feature tapped
                // Also used in placing the pin
                poiTapPoint = floatArrayOf(positionX, positionY)
                if (properties.contains(MAP_DATA_PROP_NAME)) {
                    poiTapName = properties.getString(MAP_DATA_PROP_NAME).toString()
                }
                if (properties.contains(MAP_DATA_PROP_SEARCHINDEX)) {
                    val searchIndex = properties.getNumber(MAP_DATA_PROP_SEARCHINDEX).toInt()
                    presenter?.onSearchResultTapped(searchIndex)
                } else {
                    if (properties.contains(MAP_DATA_PROP_ID)) {
                        val featureID = properties.getNumber(MAP_DATA_PROP_ID).toLong()
                        presenter?.onPlaceSearchRequested("osm:venue:$featureID")
                    } else {
                        if (poiTapPoint != null) {
                            presenter?.onReverseGeoRequested(poiTapPoint?.get(0)?.toFloat(),
                                    poiTapPoint?.get(0)?.toFloat())
                        }
                    }
                }
        })
        mapController?.setHttpHandler(tileHttpHandler)
        mapzenLocation?.mapController = mapController
    }

    private fun initAutoCompleteAdapter() {
        autoCompleteAdapter = AutoCompleteAdapter(this, R.layout.list_item_auto_complete)
    }

    private fun initFindMeButton() {
        findMe = MapData("find_me")
        Tangram.addDataSource(findMe)
        findMeButton.visibility = View.VISIBLE
        findMeButton.setOnClickListener({ presenter?.onFindMeButtonClick() })
    }

    private fun updateMute() {
        val routePresenter = routeModeView.routePresenter
        muteView.setMuted(!(routePresenter?.isMuted() == true))
    }

    private fun initMute() {
        updateMute()
        muteView.setOnClickListener({
            presenter?.onMuteClick()
        })
    }

    private fun initCompass() {
        compass.setOnClickListener({
            presenter?.onCompassClick()
        })
        routePreviewCompass.setOnClickListener({
            presenter?.onCompassClick()
        })
        routeModeCompass.setOnClickListener({
            presenter?.onCompassClick()
        })
    }

    private fun initCrashReportService() {
        crashReportService?.initAndStartSession(this)
    }

    override fun centerMapOnLocation(location: Location, zoom: Float) {
        mapController?.setMapPosition(location.longitude, location.latitude)
        mapController?.mapZoom = zoom
        showCurrentLocation(location)
    }

    override fun showCurrentLocation(location: Location) {
        val currentLocation = LngLat(location.longitude, location.latitude)
        val properties = com.mapzen.tangram.Properties()

        findMe?.clear()
        findMe?.addPoint(properties, currentLocation)
        mapController?.requestRender()
    }

    override fun setMapTilt(radians: Float) {
        mapController?.mapTilt = radians
    }

    override fun toggleMute() {
        //Update cached value of routePresenter.isMuted
        val routePresenter = routeModeView.routePresenter
        routePresenter?.onMuteClicked()

        val muted = (routePresenter?.isMuted() == true)
        muteView.setMuted(!muted)

        //Actually mute or unmute the speakerbox based on current value
        if (muted) {
            routeModeView.voiceNavigationController?.mute()
        } else {
            routeModeView.voiceNavigationController?.unmute()
        }
    }

    override fun setMapRotation(radians: Float) {
        mapController?.setMapRotation(radians, 1f)
        compass.reset()
        routePreviewCompass.reset()
        routeModeCompass.reset()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        optionsMenu = menu

        val searchView = PeliasSearchView(this)
        supportActionBar.setCustomView(searchView,ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT))
        supportActionBar.displayOptions = supportActionBar.displayOptions or
                ActionBar.DISPLAY_SHOW_CUSTOM
        val listView = findViewById(R.id.auto_complete) as AutoCompleteListView
        val emptyView = findViewById(android.R.id.empty)
        listView.hideHeader()

        if (searchView is PeliasSearchView) {
            this.searchView = searchView
            searchView.setRecentSearchIconResourceId(R.drawable.ic_recent)
            searchView.setAutoCompleteIconResourceId(R.drawable.ic_pin_c)
            listView.adapter = autoCompleteAdapter
            val pelias = Pelias.getPelias()
            pelias.setLocationProvider(presenter?.getPeliasLocationProvider())
            pelias.setApiKey(BuildConfig.PELIAS_API_KEY)
            searchView.setAutoCompleteListView(listView)
            searchView.setSavedSearch(savedSearch)
            searchView.setPelias(Pelias.getPelias())
            searchView.setCallback(PeliasCallback())
            searchView.setOnSubmitListener({ presenter?.onQuerySubmit() })
            searchView.setIconifiedByDefault(false)

            searchView.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
            searchView.queryHint = "Search for place or address"
            listView.emptyView = emptyView
            restoreCurrentSearchTerm(searchView)

            searchView.setOnPeliasFocusChangeListener { view, b ->
                if (b) {
                    presenter?.onExpandSearchView()
                }
            }
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            R.id.action_settings -> { onActionSettings(); return true }
            R.id.action_view_all -> { onActionViewAll(); return true }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun onActionSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    private fun onActionViewAll() {
        presenter?.onViewAllSearchResults()
    }

    override fun showAllSearchResults(features: List<Feature>) {
        val simpleFeatures: ArrayList<SimpleFeature> = ArrayList()
        for (feature in features) {
            simpleFeatures.add(SimpleFeature.fromFeature(feature))
        }

        val intent = Intent(this, SearchResultsListActivity::class.java)
        intent.putParcelableArrayListExtra("features", simpleFeatures)
        intent.putExtra("query", searchView?.query.toString())
        startActivityForResult(intent, requestCodeSearchResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode > 0) {
            searchResultsView.setCurrentItem(resultCode - 1)
        }
    }

    private fun saveCurrentSearchTerm() {
        presenter?.currentSearchTerm = searchView?.query.toString()
    }

    private fun restoreCurrentSearchTerm(searchView: PeliasSearchView) {
        val term = presenter?.currentSearchTerm
        if (term != null) {
            searchView.setQuery(term, false)
            if (searchResultsView.visibility == View.VISIBLE) {
                searchView.clearFocus()
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
            Log.e(TAG, "Error fetching search results: " + error?.message)
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
            Log.e(TAG, "Error Reverse Geolocating: " + error?.message)
        }
    }

    inner class PlaceCallback : Callback<Result> {
        private val TAG: String = "PlaceCallback"

        override fun success(result: Result?, response: Response?) {
            presenter?.onPlaceSearchResultsAvailable(result)
        }

        override fun failure(error: RetrofitError?) {
            hideProgress()
            Log.e(TAG, "Error fetching place search results: " + error?.message)
        }
    }

    override fun showSearchResults(features: List<Feature>) {
        hideReverseGeolocateResult()
        showSearchResultsView(features)
        addSearchResultsToMap(features, 0)
        layoutAttributionAboveSearchResults()
    }

    private fun showSearchResultsView(features: List<Feature>) {
        searchResultsView.setAdapter(SearchResultsAdapter(this, features))
        searchResultsView.visibility = View.VISIBLE
        searchResultsView.onSearchResultsSelectedListener = this
    }

    private fun layoutAttributionAboveSearchResults() {
        val attributionLayoutParams = osmAttributionText.layoutParams as RelativeLayout.LayoutParams
        val bottomMargin = resources.getDimensionPixelSize(R.dimen.padding_vertical_big)
        val searchHeight = resources.getDimensionPixelSize(R.dimen.search_results_pager_height)
        attributionLayoutParams.bottomMargin = searchHeight + bottomMargin
    }

    override fun showReverseGeocodeFeature(features: List<Feature>) {
        var poiTapFallback = false
        if (poiTapPoint != null) {
            // Fallback for a failed Pelias Place Callback
            overridePlaceFeature(features.get(0))
            poiTapFallback = true
        }

        showPlaceSearchFeature(features)

        if (poiTapFallback) return

        val simpleFeature = SimpleFeature.fromFeature(features.get(0))
        val lngLat = LngLat(simpleFeature.lng(), simpleFeature.lat())

        val properties = com.mapzen.tangram.Properties()
        properties.set(MAP_DATA_PROP_STATE, MAP_DATA_PROP_STATE_ACTIVE)
        if (reverseGeocodeData == null) {
            reverseGeocodeData = MapData("reverse_geocode")
            Tangram.addDataSource(reverseGeocodeData)
        }
        reverseGeocodeData?.clear()
        reverseGeocodeData?.addPoint(properties, lngLat)

        mapController?.requestRender()
    }

    override fun drawTappedPoiPin() {
        hideSearchResultsView()
        layoutAttributionAlignBottom()

        var lngLat: LngLat? = null

        val pointX = poiTapPoint?.get(0)?.toDouble()
        val pointY = poiTapPoint?.get(1)?.toDouble()
        if (pointX != null && pointY != null) {
            lngLat = mapController?.coordinatesAtScreenPosition(pointX, pointY)
        }

        val properties = com.mapzen.tangram.Properties()
        properties.set(MAP_DATA_PROP_STATE, MAP_DATA_PROP_STATE_ACTIVE)

        // hijack reverseGeocodeData for tappedPoiPin
        if (reverseGeocodeData == null) {
            reverseGeocodeData = MapData("reverse_geocode")
            Tangram.addDataSource(reverseGeocodeData)
        }
        reverseGeocodeData?.clear()
        reverseGeocodeData?.addPoint(properties, lngLat)

        mapController?.requestRender()
    }

    override fun showPlaceSearchFeature(features: List<Feature>) {
        searchResultsView.setAdapter(SearchResultsAdapter(this, features.subList(0, 1)))
        searchResultsView.visibility = View.VISIBLE
        searchResultsView.onSearchResultsSelectedListener = this
    }

    override fun addSearchResultsToMap(features: List<Feature>, activeIndex: Int) {
        centerOnCurrentFeature(features)

        if (searchResults == null) {
            searchResults = MapData("search")
            Tangram.addDataSource(searchResults)
        }

        var featureCount: Int = 0
        searchResults?.clear()
        for (feature in features) {
            val simpleFeature = SimpleFeature.fromFeature(feature)
            val lngLat = LngLat(simpleFeature.lng(), simpleFeature.lat())
            val properties = com.mapzen.tangram.Properties()
            properties.set(MAP_DATA_PROP_SEARCHINDEX, featureCount.toDouble());
            if (featureCount == activeIndex) {
                properties.set(MAP_DATA_PROP_STATE, MAP_DATA_PROP_STATE_ACTIVE)
            } else {
                properties.set(MAP_DATA_PROP_STATE, MAP_DATA_PROP_STATE_INACTIVE);
            }
            searchResults?.addPoint(properties, lngLat)
            featureCount++
        }
        mapController?.requestRender()
    }

    override fun centerOnCurrentFeature(features: List<Feature>) {
        centerOnFeature(features, searchResultsView.getCurrentItem())
    }

    override fun centerOnFeature(features: List<Feature>, position: Int) {
        Handler().postDelayed({
            if(features.size > 0) {
                searchResultsView.setCurrentItem(position)
                val feature = SimpleFeature.fromFeature(features[position])
                mapController?.setMapPosition(feature.lng(), feature.lat(), 1f)
                mapController?.mapZoom = MainPresenter.DEFAULT_ZOOM
            }
        }, 100)
    }

    override fun placeSearch(gid: String) {
        val pelias = Pelias.getPelias()
        pelias.setLocationProvider(presenter?.getPeliasLocationProvider())
        pelias?.place(gid, (PlaceCallback()))
    }

    override fun emptyPlaceSearch() {
        if (poiTapPoint != null) {
            presenter?.onReverseGeoRequested(poiTapPoint?.get(0)?.toFloat(), poiTapPoint?.get(1)?.toFloat())
        }
    }

    override fun reverseGeolocate(screenX: Float, screenY: Float) {
        val pelias = Pelias.getPelias()
        pelias.setLocationProvider(presenter?.getPeliasLocationProvider())
        var coords = mapController?.coordinatesAtScreenPosition(screenX.toDouble(), screenY.toDouble())
        presenter?.currentFeature = getGenericLocationFeature(coords?.latitude as Double,
                coords?.longitude as Double)
        pelias.reverse(coords?.latitude as Double, coords?.longitude as Double,
                ReversePeliasCallback())
    }

    override fun hideReverseGeolocateResult() {
        reverseGeocodeData?.clear()
    }

    override fun hideSearchResults() {
        hideSearchResultsView()
        layoutAttributionAlignBottom()
        searchResults?.clear()
    }

    private fun hideSearchResultsView() {
        searchResultsView.visibility = View.GONE
    }

    private fun layoutAttributionAlignBottom() {
        val attributionLayoutParams = osmAttributionText.layoutParams as RelativeLayout.LayoutParams

        val bottomMargin = resources.getDimensionPixelSize(R.dimen.padding_vertical_big)
        attributionLayoutParams.bottomMargin = bottomMargin
    }

    override fun showProgress() {
        findViewById(R.id.progress).visibility = View.VISIBLE
    }

    override fun hideProgress() {
        findViewById(R.id.progress).visibility = View.GONE
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
        presenter?.onCollapseSearchView()
    }

    override fun clearQuery() {
        searchView?.setQuery("", false)
    }

    override fun showRoutePreview(location: Location, feature: Feature) {
        routeManager?.origin = location
        routeManager?.destination = feature

        if (location.hasBearing()) {
            routeManager?.bearing = location.bearing
        } else {
            routeManager?.bearing = null
        }

        routePreviewView.destination = SimpleFeature.fromFeature(feature)
        route()
    }

    override fun drawRoute(route: Route) {
        routeModeView.drawRoute(route)
    }

    override fun clearRoute() {
        routeModeView.clearRoute()
    }

    override fun success(route: Route) {
        routeManager?.route = route
        routePreviewView.route = route
        runOnUiThread ({
            if (routeModeView.visibility != View.VISIBLE) {
                supportActionBar?.hide()
                routePreviewView.visibility = View.VISIBLE
                findViewById(R.id.route_preview_distance_time_view).visibility = View.VISIBLE
                zoomToShowRoute(route.getGeometry().toTypedArray())
            }
        })
        updateRoutePreview()
        routeModeView.drawRoute(route)
        hideProgress()
    }

    private fun zoomToShowRoute(route: Array<Location>) {

        // Make sure we have some points to work with
        if (route.isEmpty()) {
            return
        }

        mapController?.mapRotation = 0f
        mapController?.mapTilt = 0f

        // Determine the smallest axis-aligned box that contains the route longitude and latitude
        val start = route.first()
        val finish = route.last()
        var routeBounds = AxisAlignedBoundingBox()
        routeBounds.center = PointD(start.longitude, start.latitude)

        for (p in route) {
            routeBounds.expandTo(p.longitude, p.latitude)
        }

        // Add some padding to the box
        val padding = 0.35
        routeBounds.width = (1.0 + padding) * routeBounds.width
        routeBounds.height = (1.0 + padding) * routeBounds.height

        // Determine the bounds of the current view in longitude and latitude
        val screen = Point()
        windowManager.defaultDisplay.getSize(screen)
        val viewMin = mapController?.coordinatesAtScreenPosition(0.0, 0.0) ?: LngLat()
        val viewMax = mapController?.coordinatesAtScreenPosition(screen.x.toDouble(), screen.y.toDouble()) ?: LngLat()

        // Determine the amount of re-scaling needed to view the route
        val scaleX = routeBounds.width / (viewMax.longitude - viewMin.longitude)
        val scaleY = routeBounds.height / (viewMax.latitude - viewMin.latitude)
        val zoomDelta = -Math.log(Math.max(scaleX, scaleY)) / Math.log(2.0)

        // Update map position and zoom
        var map = mapController
        if (map != null) {
            val z = map.mapZoom + zoomDelta.toFloat()
            map.mapZoom = z
            if (map.mapZoom == z) {
                // If the new zoom would go beyond the bounds of the earth, the value
                // won't be set - so we want to make sure that it changed before moving
                // the position.
                map.mapPosition = LngLat(routeBounds.center.x, routeBounds.center.y)
            }
        }

        hideRoutePins()
        showRoutePins(LngLat(start.longitude, start.latitude),
                LngLat(finish.longitude, finish.latitude))
    }

    private fun showRoutePins(start: LngLat, end: LngLat) {
        startPin = MapData("route_start")
        endPin = MapData("route_stop")
        Tangram.addDataSource(startPin)
        Tangram.addDataSource(endPin)

        val properties = com.mapzen.tangram.Properties()
        startPin?.addPoint(properties, start)
        endPin?.addPoint(properties, end)
        mapController?.requestRender()
    }

    private fun handleRouteFailure() {
        hideRoutePins()
        routeModeView.hideRouteLine()

        val origin = routeManager?.origin
        val destination = routeManager?.destination
        if (origin is Location && destination is Feature) {
            val destinationFeature = SimpleFeature.fromFeature(destination)
            val start = LngLat(origin.longitude, origin.latitude)
            val end = LngLat(destinationFeature.lng(), destinationFeature.lat())
            showRoutePins(start, end)

            val startLocation = origin
            val endLocation = Location(origin)
            endLocation.longitude = end.longitude
            endLocation.latitude = end.latitude
            zoomToShowRoute(arrayOf(startLocation, endLocation))
        }
    }

    override fun failure(statusCode: Int) {
        runOnUiThread ({
            if (routeModeView.visibility != View.VISIBLE) {
                supportActionBar?.hide()
                routePreviewView.visibility = View.VISIBLE
                findViewById(R.id.route_preview_distance_time_view).visibility = View.GONE
                handleRouteFailure()
            }
        })

        updateRoutePreview()
        hideProgress()
        Toast.makeText(this@MainActivity, "No route found", Toast.LENGTH_LONG).show()
    }

    override fun hideRoutePreview() {
        if((findViewById(R.id.route_mode) as RouteModeView).visibility != View.VISIBLE) {
            supportActionBar?.show()
            routeManager?.reverse = false
            findViewById(R.id.route_preview).visibility = View.GONE
            hideRoutePins()
        }
    }

    private fun route() {
        showProgress()
        routeManager?.fetchRoute(this)
    }

    private fun updateRoutePreview() {
        byCar.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                routeManager?.type = Router.Type.DRIVING
                route()
            }
        }

        byBike.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                routeManager?.type = Router.Type.BIKING
                route()
            }
        }

        byFoot.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                routeManager?.type = Router.Type.WALKING
                route()
            }
        }
    }

    private fun reverse() {
        routeManager?.toggleReverse()
        routePreviewView.reverse = routeManager?.reverse ?: false
        route()
    }

    private fun initReverseButton() {
        reverseButton.setOnClickListener({ reverse() })
        viewListButton.setOnClickListener({ presenter?.onClickViewList() })
        startNavigationButton.setOnClickListener({ presenter?.onClickStartNavigation() })
    }

    override fun onBackPressed() {
        if(findViewById(R.id.route_mode).visibility == View.VISIBLE) {
            routeModeView.notificationCreator?.killNotification()
        }
        presenter?.onBackPressed()
    }

    override fun shutDown() {
        finish()
    }

    override fun showDirectionList() {
        val instructionStrings = ArrayList<String>()
        val instructionType = ArrayList<Int>()
        val instructionDistance = ArrayList<Int>()
        val instructions = routeManager?.route?.getRouteInstructions()
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

        val simpleFeature = SimpleFeature.fromFeature(routeManager?.destination)
        val intent = Intent(this, InstructionListActivity::class.java)
        intent.putExtra(InstructionListActivity.EXTRA_STRINGS, instructionStrings)
        intent.putExtra(InstructionListActivity.EXTRA_TYPES, instructionType)
        intent.putExtra(InstructionListActivity.EXTRA_DISTANCES, instructionDistance)
        intent.putExtra(InstructionListActivity.EXTRA_DESTINATION, simpleFeature.name())
        intent.putExtra(InstructionListActivity.EXTRA_REVERSE, routeManager?.reverse)
        startActivityForResult(intent, requestCodeSearchResults)
    }

    override fun startRoutingMode(feature: Feature) {
        resetMute()
        showRoutingMode(feature)
        routeModeView.startRoute(feature, routeManager?.route)
        setRoutingCamera()
        hideRoutePins()
    }

    override fun resumeRoutingMode(feature: Feature) {
        showRoutingMode(feature)
        val route = routeManager?.route
        if (route is Route) {
            drawRoute(route)
        }
        routeModeView.resumeRoute(feature, routeManager?.route)
    }

    private fun setRoutingCamera() {
        mapController?.setMapCameraType(MapController.CameraType.PERSPECTIVE)
    }

    private fun setDefaultCamera() {
        mapController?.setMapCameraType(MapController.CameraType.ISOMETRIC)
    }

    private fun resetMute() {
        val routePresenter = routeModeView.routePresenter
        routePresenter?.setMuted(false)
    }

    private fun showRoutingMode(feature: Feature) {
        hideFindMe()
        supportActionBar?.hide()
        updateMute()
        routeManager?.destination = feature
        routeManager?.reverse = false
        routePreviewView.visibility = View.GONE
        routeModeView.mainPresenter = presenter
        routeModeView.mapController = mapController
        presenter?.routeViewController = routeModeView
        routeModeView.voiceNavigationController = VoiceNavigationController(this)
        routeModeView.notificationCreator = NotificationCreator(this)
    }

    override fun hideRoutingMode() {
        setDefaultCamera()
        initFindMeButton()
        presenter?.routingEnabled = false
        routeModeView.visibility = View.GONE
        val location = routeManager?.origin
        val feature = routeManager?.destination
        if (location is Location && feature is Feature) {
            showRoutePreview(location, feature)
        }
        supportActionBar?.hide()
        routeModeView.route = null
        routeModeView.hideRouteIcon()
        hideReverseGeolocateResult()
    }

    override fun overridePlaceFeature(feature: Feature) {
        if (poiTapPoint != null) {
            val geometry = Geometry()
            val coordinates = ArrayList<Double>()
            val pointX = poiTapPoint?.get(0)?.toDouble()
            val pointY = poiTapPoint?.get(1)?.toDouble()
            if (pointX != null && pointY != null) {
                var coords = mapController?.coordinatesAtScreenPosition(pointX, pointY)
                var lng = coords?.longitude
                var lat = coords?.latitude
                if (lng != null && lat!= null) {
                    coordinates.add(lng)
                    coordinates.add(lat)
                    geometry.coordinates = coordinates
                    feature.geometry = geometry
                }
            }
        }
        if (poiTapName != null) {
            feature.properties.name = poiTapName
        }
        poiTapName = null
        poiTapPoint = null
    }

    private fun exitNavigation() {
        initFindMeButton()
        routeModeView.voiceNavigationController?.stop()
        presenter?.routingEnabled = false
        routeModeView.clearRoute()
        routeModeView.route = null
        routeModeView.hideRouteIcon()
        routeModeView.visibility = View.GONE
        supportActionBar?.show()
        findViewById(R.id.route_preview).visibility = View.GONE
        presenter?.onExitNavigation()
        mapController?.setPanResponder(null)
    }

    private fun getGenericLocationFeature(lat: Double, lon: Double) : Feature {
        var nameLength: Int = 6
        val feature = Feature()
        val properties = Properties()
        if (lat.toString().length > nameLength && lon.toString().length > nameLength + 1) {
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

    private fun hideFindMe() {
        findMe?.clear()
        findMe = null
        findViewById(R.id.find_me).visibility = View.GONE
    }

    private fun hideRoutePins() {
        startPin?.clear()
        endPin?.clear()
        startPin = null
        endPin = null
    }
}
