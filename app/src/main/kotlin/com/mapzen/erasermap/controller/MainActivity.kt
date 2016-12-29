package com.mapzen.erasermap.controller

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Point
import android.graphics.PointF
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.ActionBar
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.Toast
import com.mapzen.android.graphics.MapView
import com.mapzen.android.graphics.MapzenMap
import com.mapzen.android.graphics.model.BubbleWrapStyle
import com.mapzen.android.graphics.model.CameraType
import com.mapzen.android.graphics.model.CinnabarStyle
import com.mapzen.android.graphics.model.MapStyle
import com.mapzen.android.graphics.model.RefillStyle
import com.mapzen.android.graphics.model.WalkaboutStyle
import com.mapzen.android.graphics.model.ZincStyle
import com.mapzen.android.lost.api.Status
import com.mapzen.android.search.MapzenSearch
import com.mapzen.erasermap.CrashReportService
import com.mapzen.erasermap.EraserMapApplication
import com.mapzen.erasermap.R
import com.mapzen.erasermap.model.AndroidAppSettings
import com.mapzen.erasermap.model.ApiKeys
import com.mapzen.erasermap.model.AppSettings
import com.mapzen.erasermap.model.ConfidenceHandler
import com.mapzen.erasermap.model.LostClientManager
import com.mapzen.erasermap.model.MapzenLocation
import com.mapzen.erasermap.model.PermissionManager
import com.mapzen.erasermap.model.RouteManager
import com.mapzen.erasermap.model.TileHttpHandler
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.erasermap.util.AxisAlignedBoundingBox
import com.mapzen.erasermap.util.AxisAlignedBoundingBox.PointD
import com.mapzen.erasermap.util.FeatureDisplayHelper
import com.mapzen.erasermap.util.NotificationBroadcastReceiver
import com.mapzen.erasermap.util.NotificationCreator
import com.mapzen.erasermap.view.CompassView
import com.mapzen.erasermap.view.MuteView
import com.mapzen.erasermap.view.RouteModeView
import com.mapzen.erasermap.view.RoutePreviewView
import com.mapzen.erasermap.view.SearchResultsAdapter
import com.mapzen.erasermap.view.SearchResultsView
import com.mapzen.erasermap.view.SettingsActivity
import com.mapzen.erasermap.view.Speaker
import com.mapzen.erasermap.view.VoiceNavigationController
import com.mapzen.model.ValhallaLocation
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.SimpleFeature
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.gson.Geometry
import com.mapzen.pelias.gson.Properties
import com.mapzen.pelias.gson.Result
import com.mapzen.pelias.widget.AutoCompleteAdapter
import com.mapzen.pelias.widget.AutoCompleteItem
import com.mapzen.pelias.widget.AutoCompleteListView
import com.mapzen.pelias.widget.PeliasSearchView
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.TouchInput
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.RouteCallback
import com.mapzen.valhalla.Router
import retrofit.Callback
import retrofit.RetrofitError
import retrofit.client.Response
import java.util.ArrayList
import javax.inject.Inject

class MainActivity : AppCompatActivity(), MainViewController,
        SearchViewController.OnSearchResultSelectedListener {

    companion object {
        @JvmStatic val DIRECTION_LIST_ANIMATION_DURATION = 300L
        @JvmStatic val PERMISSIONS_REQUEST: Int = 1
    }

    @Inject lateinit var savedSearch: SavedSearch
    @Inject lateinit var presenter: MainPresenter
    @Inject lateinit var crashReportService: CrashReportService
    @Inject lateinit var routeManager: RouteManager
    @Inject lateinit var settings: AppSettings
    @Inject lateinit var tileHttpHandler: TileHttpHandler
    @Inject lateinit var mapzenLocation: MapzenLocation
    @Inject lateinit var mapzenSearch: MapzenSearch
    @Inject lateinit var speaker: Speaker
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var apiKeys: ApiKeys
    @Inject lateinit var lostClientManager: LostClientManager
    @Inject lateinit var confidenceHandler: ConfidenceHandler
    @Inject lateinit var displayHelper: FeatureDisplayHelper

    lateinit var app: EraserMapApplication
    var mapzenMap : MapzenMap? = null
    var autoCompleteAdapter: AutoCompleteAdapter? = null
    var optionsMenu: Menu? = null
    var voiceNavigationController: VoiceNavigationController? = null
    var notificationCreator: NotificationCreator? = null

    var enableLocation: Boolean = false

    // activity_main
    val mapView: MapView by lazy { findViewById(R.id.map) as MapView }
    val compass: CompassView by lazy { findViewById(R.id.compass_view) as CompassView }
    val searchController: SearchViewController by lazy { findViewById(R.id.search_results) as SearchResultsView }
    val progressContainer: RelativeLayout by lazy { findViewById(R.id.progress_container) as RelativeLayout }

    // view_route_preview
    val routePreviewView: RoutePreviewView by lazy { findViewById(R.id.route_preview) as RoutePreviewView }
    val reverseButton: ImageButton by lazy { findViewById(R.id.route_reverse) as ImageButton }
    val routePreviewDistanceTimeLayout: View? by lazy { findViewById(R.id.route_preview_distance_time_view) }
    val distanceView: View? by lazy { findViewById(R.id.distance_preview) }
    val timeView: View? by lazy { findViewById(R.id.time_preview) }
    val viewListButton: Button by lazy { findViewById(R.id.view_list) as Button }
    val startNavigationButton: Button by lazy { findViewById(R.id.start_navigation) as Button }
    val byCar: RadioButton by lazy { findViewById(R.id.by_car) as RadioButton }
    val byBike: RadioButton by lazy { findViewById(R.id.by_bike) as RadioButton }
    val byFoot: RadioButton by lazy { findViewById(R.id.by_foot) as RadioButton }
    val byTransit: RadioButton by lazy { findViewById(R.id.by_transit) as RadioButton }
    val routePreviewCompass: CompassView by lazy { findViewById(R.id.route_preview_compass_view) as CompassView }

    // view_route_mode
    val routeModeView: RouteModeView by lazy {
        val view = findViewById(R.id.route_mode) as RouteModeView
        view.mainPresenter = presenter
        presenter.routeViewController = view
        view.voiceNavigationController = voiceNavigationController
        view.notificationCreator = notificationCreator
        view
    }
    val routeModeCompass: CompassView by lazy { findViewById(R.id.route_mode_compass_view) as CompassView }
    val muteView: MuteView by lazy { findViewById(R.id.route_mode_mute_view) as MuteView }

    var submitQueryOnMenuCreate: String? = null

    override public fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        app = application as EraserMapApplication
        app.component().inject(this)
        initCrashReportService()
        setContentView(R.layout.activity_main)
        presenter.mainViewController = this
        initVoiceNavigationController() // must initialize this before calling initMute
        initNotificationCreator()
        presenter.onRestoreViewState()
        initMapzenMap()
        initMute()
        initCompass()
        initReverseButton()
        initRoutePreviewModeBtns()
        permissionManager.activity = this
        if (permissionManager.permissionsRequired()) {
            permissionManager.requestPermissions()
        } else {
            permissionManager.grantPermissions()
        }
        supportActionBar?.setDisplayShowTitleEnabled(false)
        settings.initTangramDebugFlags()
        settings.initSearchResultVersion(this, savedSearch)
    }

    override fun onNewIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(NotificationCreator.EXIT_NAVIGATION, false) as Boolean) {
            presenter.onExitNavigation()
            if((intent?.getBooleanExtra(NotificationBroadcastReceiver.VISIBILITY, false)
                    as Boolean).not()) {
                moveTaskToBack(true)
            }
        } else {
            presenter.onIntentQueryReceived(intent?.data?.query)
        }
    }

    private fun initMapRotateListener() {
        mapzenMap?.setRotateResponder({ x, y, rotation -> presenter.onMapRotateEvent() })
    }

    override fun showCompass() {
        compass.visibility = View.VISIBLE
    }

    override fun rotateCompass() {
        val radians: Float = mapzenMap?.rotation as Float
        val degrees = Math.toDegrees(radians.toDouble()).toFloat()
        compass.rotation = degrees
        routePreviewCompass.rotation = degrees
        routeModeCompass.rotation = degrees
    }

    override public fun onStart() {
        super.onStart()
        savedSearch.deserialize(PreferenceManager.getDefaultSharedPreferences(this)
                .getString(SavedSearch.TAG, null))
    }

    override public fun onResume() {
        super.onResume()
        if (!permissionManager.permissionsRequired()) {
            permissionManager.grantPermissions()
        }
        presenter.onResume()
        app.onActivityResume()
        autoCompleteAdapter?.clear()
        autoCompleteAdapter?.notifyDataSetChanged()
        invalidateOptionsMenu()

        // Update Style all the time, as there's no access
        // to MapzenMap.getStyle() (yet)
        setMapStyle(settings.mapzenStyle)

        if (enableLocation) {
            enableLocation = false
            checkPermissionAndEnableLocation()
        }
    }

    override public fun onPause() {
        super.onPause()
        app.onActivityPause()
        if (mapzenMap?.isMyLocationEnabled != null && mapzenMap?.isMyLocationEnabled as Boolean
                && !presenter.routingEnabled) {
            enableLocation = true
            mapzenMap?.isMyLocationEnabled = false
            lostClientManager.disconnect()
        }
    }

    override public fun onStop() {
        super.onStop()
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putString(SavedSearch.TAG, savedSearch.serialize())
                .commit()
    }

    override public fun onDestroy() {
        super.onDestroy()
        saveCurrentSearchTerm()
        killNotifications()
        voiceNavigationController?.shutdown()
    }

    private fun initMapzenMap() {
        mapView.getMapAsync(apiKeys.apiKey, {
            this.mapzenMap = it
            configureMapzenMap()
            presenter.onRestoreMapState()
            presenter.configureMapzenMap()
            presenter.onIntentQueryReceived(intent?.data?.query)
        })
    }

    private fun configureMapzenMap() {
        mapzenMap?.setPersistMapData(true)
        mapzenMap?.setLongPressResponder({
            x, y ->
            confidenceHandler.longPressed = true
            presenter.onReverseGeoRequested(x, y)
        })
        mapzenMap?.tapResponder = object: TouchInput.TapResponder {
            override fun onSingleTapUp(x: Float, y: Float): Boolean = false
            override fun onSingleTapConfirmed(x: Float, y: Float): Boolean {
                confidenceHandler.longPressed = false
                presenter.onMapPressed(x, y)
                return true
            }
        }
        mapzenMap?.setDoubleTapResponder({ x, y ->
            confidenceHandler.longPressed = false
            presenter.onMapDoubleTapped(x, y)
            true
        })
        mapzenMap?.setLabelPickListener { labelPickResult, x, y ->
            confidenceHandler.longPressed = false
            var properties = labelPickResult.properties
            var coords = labelPickResult.coordinates
            presenter.onFeaturePicked(properties, coords, x, y)
        }
        checkPermissionAndEnableLocation()
        mapzenMap?.setFindMeOnClickListener {
            if (!permissionManager.permissionsGranted()) {
                permissionManager.showPermissionRequired()
            }
            presenter.onClickFindMe()
        }
        initMapRotateListener()
        mapzenMap?.mapController?.setHttpHandler(tileHttpHandler)
        mapzenLocation.mapzenMap = mapzenMap
        routeModeView.mapzenMap = mapzenMap
        settings.mapzenMap = mapzenMap
    }

    override fun screenPositionToLngLat(x: Float, y: Float): LngLat? {
        return mapzenMap?.screenPositionToLngLat(PointF(x, y))
    }

    private fun updateMute() {
        muteView.setMuted(voiceNavigationController?.isMuted() != true)
    }

    private fun initMute() {
        updateMute()
        muteView.setOnClickListener({
            presenter.onMuteClick()
        })
    }

    private fun initCompass() {
        compass.visibility = View.GONE
        compass.setOnClickListener({
            presenter.onCompassClick()
        })
        routePreviewCompass.setOnClickListener({
            presenter.onCompassClick()
        })
        routeModeCompass.setOnClickListener({
            presenter.onCompassClick()
        })
    }

    private fun initCrashReportService() {
        crashReportService.initAndStartSession(this)
    }

    private fun initVoiceNavigationController() {
        voiceNavigationController = VoiceNavigationController(this, speaker)
    }

    private fun initNotificationCreator() {
        notificationCreator = NotificationCreator(this)
    }

    override fun centerMapOnLocation(lngLat: LngLat, zoom: Float) {
        mapzenMap?.position = lngLat
        mapzenMap?.zoom = zoom
    }

    override fun setMapTilt(radians: Float) {
        mapzenMap?.tilt = radians
    }

    override fun toggleMute() {
        val muted = (voiceNavigationController?.isMuted() == true)
        if (muted) {
            muteView.setMuted(true)
            voiceNavigationController?.unmute()
        } else {
            muteView.setMuted(false)
            voiceNavigationController?.mute()
        }
    }

    override fun setMapRotation(radians: Float) {
        mapzenMap?.setRotation(radians, 1000)
        compass.reset()
        routePreviewCompass.reset()
        routeModeCompass.reset()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        optionsMenu = menu
        initSearchView()
        presenter.onRestoreOptionsMenu()
        return true
    }

    private fun initSearchView() {
        val searchView = PeliasSearchView(this)
        val listView = findViewById(R.id.auto_complete) as AutoCompleteListView
        val emptyView = findViewById(android.R.id.empty)
        val locationProvider = presenter.getPeliasLocationProvider()
        val apiKeys = apiKeys
        val callback = PeliasCallback()

        addSearchViewToActionBar(searchView)
        searchController.mainController = this
        searchController.initSearchView(searchView, listView, emptyView, presenter, locationProvider, apiKeys, callback)
        if (submitQueryOnMenuCreate != null) {
            searchView.setQuery(submitQueryOnMenuCreate, true)
            submitQueryOnMenuCreate = null
        }
    }

    private fun addSearchViewToActionBar(searchView: PeliasSearchView) {
        supportActionBar?.setCustomView(searchView, ActionBar.LayoutParams(
                ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT))
        val displayOptions = supportActionBar?.displayOptions ?: 0
        supportActionBar?.displayOptions = displayOptions or ActionBar.DISPLAY_SHOW_CUSTOM
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val cacheSearches = prefs.getBoolean(AndroidAppSettings.KEY_CACHE_SEARCH_HISTORY, true)
        searchController.searchView?.setCacheSearchResults(cacheSearches)
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
        presenter.onViewAllSearchResultsList()
    }

    override fun toggleShowAllSearchResultsList(features: List<Feature>?) {
        if (features == null) {
            return
        }

        if (presenter.resultListVisible) {
            onCloseAllSearchResultsList()
            searchController.enableSearch()
        } else {
            onShowAllSearchResultsList(features)
        }
    }

    override fun onShowAllSearchResultsList(features: List<Feature>) {
        saveCurrentSearchTerm()
        presenter.resultListVisible = true
        optionsMenu?.findItem(R.id.action_view_all)?.setIcon(R.drawable.ic_map)

        val simpleFeatures: ArrayList<AutoCompleteItem> = ArrayList()
        for (feature in features) {
            simpleFeatures.add(AutoCompleteItem(SimpleFeature.fromFeature(feature)))
        }
        searchController.searchView?.disableAutoKeyboardShow()
        searchController.searchView?.disableAutoComplete()
        searchController.searchView?.onActionViewExpanded()
        searchController.searchView?.setQuery(presenter.currentSearchTerm, false)
        val autoCompleteAdapter = searchController.autoCompleteListView?.adapter as AutoCompleteAdapter
        autoCompleteAdapter.clear()
        autoCompleteAdapter.addAll(simpleFeatures)
        autoCompleteAdapter.notifyDataSetChanged()
        searchController.autoCompleteListView?.setOnItemClickListener { parent, view, position, id ->
            (findViewById(R.id.search_results) as SearchResultsView).setCurrentItem(position)
            onCloseAllSearchResultsList()

        }
        searchController.disableSearch()
    }

    override fun onCloseAllSearchResultsList() {
        searchController.autoCompleteListView?.onItemClickListener = searchController.searchView?.OnItemClickHandler()?.invoke()
        presenter.resultListVisible = false
        optionsMenu?.findItem(R.id.action_view_all)?.setIcon(R.drawable.ic_list)
        searchController.searchView?.enableAutoKeyboardShow()
        searchController.searchView?.onActionViewCollapsed()
        searchController.searchView?.isIconified = false
        searchController.searchView?.clearFocus()
        searchController.searchView?.disableAutoComplete()
        searchController.searchView?.setQuery(presenter.currentSearchTerm, false)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode > 0) {
            searchController.setCurrentItem(resultCode - 1)
        }
    }

    private fun saveCurrentSearchTerm() {
        presenter.currentSearchTerm = searchController.searchView?.query.toString()
    }

    override fun setOptionsMenuIconToList() {
        optionsMenu?.findItem(R.id.action_view_all)?.setIcon(R.drawable.ic_list)
    }

    inner class PeliasCallback : Callback<Result> {
        private val TAG: String = "PeliasCallback"

        override fun success(result: Result?, response: Response?) {
            presenter.reverseGeoLngLat = null
            presenter.onSearchResultsAvailable(result)
            setOptionsMenuIconToList()
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
            presenter.onReverseGeocodeResultsAvailable(result)
        }

        override fun failure(error: RetrofitError?) {
            hideProgress()
            Log.e(TAG, "Error Reverse Geolocating: " + error?.message)
        }
    }

    inner class PlaceCallback : Callback<Result> {
        private val TAG: String = "PlaceCallback"

        override fun success(result: Result?, response: Response?) {
            presenter.onPlaceSearchResultsAvailable(result)
        }

        override fun failure(error: RetrofitError?) {
            hideProgress()
            Log.e(TAG, "Error fetching place search results: " + error?.message)
        }
    }

    override fun showSearchResultsView(features: List<Feature>) {
        searchController.setSearchResultsAdapter(SearchResultsAdapter(this, features, displayHelper,
                permissionManager))
        searchController.showSearchResults()
        searchController.onSearchResultsSelectedListener = this
    }

    private fun baseAttributionParams(): RelativeLayout.LayoutParams {
        val layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT)
        val margin = resources.getDimensionPixelSize(R.dimen.mz_attribution_margin_bottom)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.leftMargin = margin
        return layoutParams
    }

    override fun layoutAttributionAlignBottom() {
        val layoutParams = baseAttributionParams()
        val margin = resources.getDimensionPixelSize(R.dimen.em_attribution_margin_bottom)
        layoutParams.bottomMargin = margin
        mapView.attribution.layoutParams = layoutParams
    }

    private fun baseFindMeParams(): RelativeLayout.LayoutParams {
        val scale = resources.displayMetrics.density
        val size = (44 * scale).toInt()
        val layoutParams = RelativeLayout.LayoutParams(size, size)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        val margin = resources.getDimensionPixelSize(R.dimen.mz_find_me_button_margin_bottom)
        layoutParams.rightMargin = margin
        return layoutParams
    }

    override fun layoutFindMeAlignBottom() {
        val layoutParams = baseFindMeParams()
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        val margin = resources.getDimensionPixelSize(R.dimen.em_find_me_button_margin_bottom)
        layoutParams.bottomMargin = margin
        mapView.findMe.layoutParams = layoutParams
    }

    override fun layoutAttributionAboveSearchResults(features: List<Feature>) {
        if (features.count() == 0) return
        val layoutParams = baseAttributionParams()
        val bottomMargin = resources.getDimensionPixelSize(R.dimen.em_attribution_margin_bottom)
        val searchHeight = resources.getDimensionPixelSize(R.dimen.search_results_pager_height)
        val indicator = findViewById(R.id.indicator)
        if (features.count() > 1) {
            indicator?.addOnLayoutChangeListener({ view, left, top, right, bottom, oldLeft, oldTop,
                                                   oldRight, oldBottom ->
                val indicatorHeight = bottom - top
                layoutParams.bottomMargin = searchHeight + indicatorHeight + bottomMargin
                mapView.attribution.layoutParams = layoutParams
            })
        } else {
            layoutParams.bottomMargin = searchHeight + bottomMargin
            mapView.attribution.layoutParams = layoutParams
        }
    }

    override fun layoutAttributionAboveOptions() {
        val layoutParams = baseAttributionParams()
        val optionsView = findViewById(R.id.options)
        optionsView?.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop,
                                                 oldRight, oldBottom ->
            val optionsHeight = bottom - top
            val padding = resources.getDimensionPixelSize(R.dimen.em_attribution_margin_bottom)
            layoutParams.bottomMargin = optionsHeight + padding
            mapView.attribution.layoutParams = layoutParams
        }
    }

    override fun layoutFindMeAboveSearchResults(features: List<Feature>) {
        if (features.count() == 0) return
        val layoutParams = baseFindMeParams()
        val bottomMargin = resources.getDimensionPixelSize(R.dimen.em_find_me_button_margin_bottom)
        val searchHeight = resources.getDimensionPixelSize(R.dimen.search_results_pager_height)
        layoutParams.bottomMargin = searchHeight + bottomMargin
        mapView.findMe.layoutParams = layoutParams
    }

    override fun layoutFindMeAboveOptions() {
        val layoutParams = baseFindMeParams()
        val optionsView = findViewById(R.id.options)
        optionsView?.addOnLayoutChangeListener { view, left, top, right, bottom, oldLeft, oldTop,
                                                 oldRight, oldBottom ->
            val optionsHeight = bottom - top
            val padding = resources.getDimensionPixelSize(R.dimen.em_find_me_button_margin_bottom)
            layoutParams.bottomMargin = optionsHeight + padding
            mapView.findMe.layoutParams = layoutParams
        }
    }

    /**
     * If the current query in search view is equal to special query, toggle shared preferences
     * to show or hide debug settings in settings fragment
     */
    override fun toggleShowDebugSettings() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        val prev = preferences.getBoolean(AndroidAppSettings.KEY_SHOW_DEBUG_SETTINGS, false)
        editor.putBoolean(AndroidAppSettings.KEY_SHOW_DEBUG_SETTINGS, !prev)
        editor.commit()

        val status = resources.getString(if (prev) R.string.disabled else R.string.enabled)
        val debugToastTitle = resources.getString(R.string.debug_settings_toast_title, status)
        Toast.makeText(this, debugToastTitle, Toast.LENGTH_SHORT).show()
    }

    override fun clearSearchResults() {
        mapzenMap?.clearSearchResults()
    }

    override fun drawSearchResults(points: List<LngLat>, activeIndex: Int) {
        mapzenMap?.drawSearchResults(points, activeIndex)
    }

    override fun showPlaceSearchFeature(features: List<Feature>) {
        searchController.setSearchResultsAdapter(SearchResultsAdapter(this, features.subList(0, 1),
                displayHelper, permissionManager))
        searchController.showSearchResults()
        searchController.onSearchResultsSelectedListener = this
    }

    override fun getCurrentSearchPosition(): Int {
        return searchController.getCurrentItem()
    }

    override fun setCurrentSearchItem(position: Int) {
        searchController.setCurrentItem(position)
    }

    override fun setMapPosition(lngLat: LngLat, duration: Int) {
        mapzenMap?.setPosition(lngLat, duration)
    }

    override fun setMapZoom(zoom: Float) {
        mapzenMap?.zoom = zoom
    }
    override fun setMapZoom(zoom: Float, duration: Int) {
        mapzenMap?.setZoom(zoom, duration)
    }

    override fun setMapStyle(styleKey: String) {
        val index = resources.getStringArray(R.array.mapzen_styles_values).indexOf(styleKey)
        Log.i("MainActivity", "Style = " + styleKey + "; Index = " + index)

        var mapStyle:MapStyle
        when (index) {
            0 -> mapStyle = BubbleWrapStyle()
            1 -> mapStyle = RefillStyle()
            2 -> mapStyle = WalkaboutStyle()
            3 -> mapStyle = CinnabarStyle()
            4 -> mapStyle = ZincStyle()
            else -> mapStyle = BubbleWrapStyle()
        }
        mapzenMap?.setStyle(mapStyle)
    }

    override fun placeSearch(gid: String) {
        mapzenSearch.setLocationProvider(presenter.getPeliasLocationProvider())
        mapzenSearch.place(gid, (PlaceCallback()))
    }

    override fun reverseGeolocate(screenX: Float, screenY: Float) {
        mapzenSearch.setLocationProvider(presenter.getPeliasLocationProvider())
        val coords = mapzenMap?.screenPositionToLngLat(PointF(screenX, screenY))
        presenter.reverseGeoLngLat = coords
        presenter.currentFeature = getGenericLocationFeature(coords?.latitude as Double,
            coords?.longitude as Double)
        mapzenSearch.reverse(coords?.latitude as Double, coords?.longitude as Double,
            ReversePeliasCallback())
    }

    override fun hideReverseGeolocateResult() {
        mapzenMap?.clearDroppedPins()
    }

    override fun showReverseGeoResult(lngLat: LngLat?) {
        mapzenMap?.drawDroppedPin(lngLat)
    }

    override fun hideSearchResults() {
        hideSearchResultsView()
        layoutAttributionAlignBottom()
        layoutFindMeAlignBottom()
        clearSearchResults()
    }

    override fun hideSearchResultsView() {
        searchController.hideSearchResults()
    }

    override fun showProgress() {
        progressContainer.visibility = View.VISIBLE
    }

    override fun hideProgress() {
        progressContainer.visibility = View.GONE
    }

    override fun onSearchResultSelected(position: Int) {
        presenter.onSearchResultSelected(position)
    }

    override fun showActionViewAll() {
        optionsMenu?.findItem(R.id.action_view_all)?.setVisible(true)
    }

    override fun hideActionViewAll() {
        optionsMenu?.findItem(R.id.action_view_all)?.setVisible(false)
    }

    override fun collapseSearchView() {
        presenter.onCollapseSearchView()
    }

    override fun expandSearchView() {
        presenter.onExpandSearchView()
    }

    override fun clearQuery() {
        searchController.searchView?.setQuery("", false)
    }

    override fun hideSettingsBtn() {
        optionsMenu?.findItem(R.id.action_settings)?.isVisible = false
    }

    override fun showSettingsBtn() {
        optionsMenu?.findItem(R.id.action_settings)?.setVisible(true)
    }

    override fun showRoutePreviewDestination(destination: SimpleFeature) {
        routePreviewView.destination = destination
    }

    override fun drawRoute(route: Route) {
        routeModeView.drawRoute(route, routeManager.type)
    }

    override fun clearRoute() {
        routeModeView.clearRoute()
    }

    override fun hideActionBar() {
        supportActionBar?.hide()
    }

    override fun showRoutePreviewView() {
        routePreviewView.visibility = View.VISIBLE
    }

    override fun showRoutePreviewDistanceTimeLayout() {
        routePreviewDistanceTimeLayout?.visibility = View.VISIBLE
        distanceView?.visibility = View.VISIBLE
        timeView?.visibility = View.VISIBLE
    }

    private fun onRouteSuccess(route: Route) {
        presenter.onRouteSuccess(route)
    }

    private fun zoomToShowRoute(route: Array<ValhallaLocation>) {

        // Make sure we have some points to work with
        if (route.isEmpty()) {
            return
        }

        mapzenMap?.rotation = 0f
        mapzenMap?.tilt = 0f

        // Determine the smallest axis-aligned box that contains the route longitude and latitude
        val start = route.first()
        val finish = route.last()
        val routeBounds = AxisAlignedBoundingBox()
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
        // Take half of the y dimension to allow space for other UI elements
        val viewMin = mapzenMap?.screenPositionToLngLat(PointF(0.0f, screen.y.toFloat() * 0.25f)) ?: LngLat()
        val viewMax = mapzenMap?.screenPositionToLngLat(PointF(screen.x.toFloat(), screen.y.toFloat() * 0.75f)) ?: LngLat()

        // Determine the amount of re-scaling needed to view the route
        val scaleX = routeBounds.width / Math.abs(viewMax.longitude - viewMin.longitude)
        val scaleY = routeBounds.height / Math.abs(viewMax.latitude - viewMin.latitude)
        val zoomDelta = -Math.log(Math.max(scaleX, scaleY)) / Math.log(2.0)

        // Update map position and zoom
        val map = mapzenMap
        if (map != null) {
            val z = map.zoom + zoomDelta.toFloat()
            map.zoom = z
            if (map.zoom == z) {
                // If the new zoom would go beyond the bounds of the earth, the value
                // won't be set - so we want to make sure that it changed before moving
                // the position.
                map.position = LngLat(routeBounds.center.x, routeBounds.center.y)
            }
        }

        hideMapRoutePins()
        showRoutePins(LngLat(start.longitude, start.latitude),
                LngLat(finish.longitude, finish.latitude))
    }

    private fun showRoutePins(start: LngLat, end: LngLat) {
        mapzenMap?.drawRoutePins(start, end)
    }

    private fun handleRouteFailure() {
        hideMapRoutePins()
        routeModeView.hideRouteLine()

        val origin = routeManager.origin
        val destination = routeManager.destination
        if (origin is ValhallaLocation && destination is Feature) {
            val destinationFeature = SimpleFeature.fromFeature(destination)
            val start = LngLat(origin.longitude, origin.latitude)
            val end = LngLat(destinationFeature.lng(), destinationFeature.lat())
            showRoutePins(start, end)

            val startLocation = origin
            val endLocation = ValhallaLocation(origin)
            endLocation.longitude = end.longitude
            endLocation.latitude = end.latitude
            zoomToShowRoute(arrayOf(startLocation, endLocation))
        }
    }

    private fun onRouteFailure(statusCode: Int) {
        runOnUiThread ({
            if (routeModeView.visibility != View.VISIBLE) {
                supportActionBar?.hide()
                routePreviewView.visibility = View.VISIBLE
                routePreviewDistanceTimeLayout?.visibility = View.GONE
                distanceView?.visibility = View.GONE
                timeView?.visibility = View.GONE
                handleRouteFailure()
            }
        })
        hideProgress()
        routePreviewView.disableStartNavigation()
    }

    override fun hideRoutePreviewView() {
        routePreviewView.visibility = View.GONE
    }

    override fun showActionBar() {
        supportActionBar?.show()
    }

    override fun route() {
        presenter.onRouteRequest(CancelableRouteCallback())
    }

    override fun cancelRouteRequest() {
        val callback = routeManager.currentRequest
        if (callback is CancelableRouteCallback) {
            callback.isCanceled = true
        }
    }

    override fun restoreRoutePreviewButtons() {
        routePreviewView.restore(routeManager)
    }

    private fun reverse() {
        routeManager.toggleReverse()
        routePreviewView.reverse = routeManager.reverse
        route()
    }

    private fun initReverseButton() {
        reverseButton.setOnClickListener({ reverse() })
        viewListButton.setOnClickListener({ presenter.onClickViewList() })
        startNavigationButton.setOnClickListener({
            layoutAttributionAlignBottom()
            presenter.onClickStartNavigation()
        })
    }

    private fun initRoutePreviewModeBtns() {
        byCar.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed && isChecked) {
                routeManager.type = Router.Type.DRIVING
                route()
            }
        }

        byBike.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed && isChecked) {
                routeManager.type = Router.Type.BIKING
                route()
            }
        }

        byFoot.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed && isChecked) {
                routeManager.type = Router.Type.WALKING
                route()
            }
        }

        byTransit.setOnCheckedChangeListener { buttonView, isChecked ->
            if (buttonView.isPressed && isChecked) {
                routeManager.type = Router.Type.MULTIMODAL
                route()
            }
        }
    }

    private fun killNotifications() {
        routeModeView.notificationCreator?.killNotification()
    }

    override fun onBackPressed() {
        if(findViewById(R.id.route_mode)?.visibility == View.VISIBLE) {
            killNotifications()
        }
        presenter.onBackPressed()
    }

    override fun shutDown() {
        finish()
    }

    override fun showDirectionsList() {
        routePreviewView.showDirectionsListView(routeManager, windowManager, compass)
    }

    override fun hideDirectionsList() {
        routePreviewView.hideDirectionsListView(windowManager, compass)
    }

    /**
     * Create and/or update routing mode UI.
     *
     * @param feature the current destination.
     * @param isNew true if the call is for a new route; false if this call is for a reroute.
     */
    override fun startRoutingMode(feature: Feature, isNew: Boolean) {
        if (isNew) {
            // Resume button should be hidden at the start of a new route.
            routeModeView.hideResumeButton()
        }

        // Set camera before RouteModeView#startRoute so that MapzenMap#sceneUpdate called
        // before MapzenMap#queueEvent
        setRoutingCamera()
        if (confidenceHandler.useRawLatLng(feature.properties.confidence)) {
            val rawFeature = presenter.generateRawFeature()
            showRoutingMode(rawFeature)
            routeModeView.startRoute(rawFeature, routeManager.route)
        } else {
            showRoutingMode(feature)
            routeModeView.startRoute(feature, routeManager.route)
        }
        hideMapRoutePins()
    }

    override fun resetMute() {
        voiceNavigationController?.unmute()
        muteView.setMuted(true)
    }

    override fun resumeRoutingMode(feature: Feature) {
        showRoutingMode(feature)
        routeModeView.resumeRoute(feature, routeManager.route)
    }

    override fun resumeRoutingModeForMap() {
        routeModeView.resumeRouteForMap()
    }

    private fun setRoutingCamera() {
        if (routeModeView.isResumeButtonHidden()) {
            mapzenMap?.cameraType = CameraType.PERSPECTIVE
        }
    }

    override fun setDefaultCamera() {
        mapzenMap?.cameraType = CameraType.ISOMETRIC
    }

    private fun showRoutingMode(feature: Feature) {
        hideFindMe()
        supportActionBar?.hide()
        updateMute()
        routeManager.destination = feature
        routeManager.reverse = false
        routePreviewView.visibility = View.GONE
    }

    override fun hideRoutingMode() {
        presenter.routingEnabled = false
        setDefaultCamera()
        checkPermissionAndEnableLocation()
        hideRouteModeView()
        supportActionBar?.hide()
        routeModeView.route = null
        hideRouteIcon()
        routeModeView.hideResumeButton()
        hideReverseGeolocateResult()
    }

    override fun hideRouteIcon() {
        routeModeView.hideRouteIcon()
    }

    override fun hideRouteModeView() {
        routeModeView.visibility = View.GONE
    }

    override fun stopSpeaker() {
        voiceNavigationController?.stop()
    }

    private fun getGenericLocationFeature(lat: Double, lon: Double) : Feature {
        val nameLength: Int = 6
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
        mapzenMap?.isMyLocationEnabled = false
    }

    override fun hideMapRoutePins() {
        mapzenMap?.clearRoutePins()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
            grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST -> {
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionManager.grantPermissions()
                    checkPermissionAndEnableLocation()
                    val findMe = mapView.findViewById(R.id.mz_find_me)
                    findMe.callOnClick()
                }
            }
        }
    }

    override fun checkPermissionAndEnableLocation() {
        presenter.checkPermissionAndEnableLocation()
    }

    override fun setMyLocationEnabled(enabled: Boolean) {
        mapzenMap?.isMyLocationEnabled = enabled
    }

    override fun executeSearch(query: String) {
        if (searchController.searchView != null) {
            searchController.searchView?.setQuery(query, true)
        } else {
            submitQueryOnMenuCreate = query
        }
    }

    override fun deactivateFindMeTracking() {
        mapView.findMe.isActivated = false
    }

    override fun handleLocationResolutionRequired(status: Status) {
        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.location_required_title)
            .setMessage(R.string.location_required_msg)
            .setNegativeButton(R.string.no, null)
            .setPositiveButton(R.string.yes) { dialog, which ->
                status.startResolutionForResult(this, 0)
            }
            .create()
        dialog.show()
    }

    override fun updateRoutePreviewStartNavigation() {
        routePreviewView.enableStartNavigation(routeManager.type, routeManager.reverse)
    }

    override fun setRoutePreviewViewRoute(route: Route) {
        routePreviewView.route = route
    }

    override fun showRoutePinsOnMap(locations: Array<ValhallaLocation>) {
        zoomToShowRoute(locations)
    }

    inner class CancelableRouteCallback : RouteCallback {
        var isCanceled: Boolean = false

        override fun success(route: Route) {
            if (!isCanceled) {
                onRouteSuccess(route)
            }
        }

        override fun failure(statusCode: Int) {
            if (!isCanceled) {
                onRouteFailure(statusCode)
            }
        }
    }

    override fun stopVoiceNavigationController() {
        routeModeView.voiceNavigationController?.stop()
    }

    override fun resetMapPanResponder() {
        mapzenMap?.panResponder = null
    }

    /**
     * Displays a [Toast] message with the given string ID.
     */
    override fun toastify(resId: Int) {
        Toast.makeText(this, resId, Toast.LENGTH_LONG).show()
    }

    /**
     * Requests focus on the [PeliasSearchView] and sets the cursor position to the end of the text.
     */
    override fun focusSearchView() {
        searchController.searchView?.requestFocus()

        // PeliasSearchView should allow cursor position to be set without performing a lookup on
        // the EditText. See https://github.com/pelias/pelias-android-sdk/issues/52
        val editText = searchController.searchView?.findViewById(R.id.search_src_text) as EditText?
        editText?.setSelection(editText.text.length)
    }

    override fun getMapPosition(): LngLat? {
        return mapzenMap?.position
    }

    override fun getMapZoom(): Float? {
        return mapzenMap?.zoom
    }
}
