package com.mapzen.erasermap.view

import android.app.AlertDialog
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.R
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.widget.PeliasSearchView
import com.mapzen.tangram.MapView
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router

import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenu
import org.robolectric.shadows.ShadowActivity
import org.robolectric.shadows.ShadowIntent
import org.robolectric.shadows.ShadowLocationManager

import android.content.Intent
import android.content.SharedPreferences
import android.location.LocationManager
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.v7.widget.SearchView
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.widget.TextView

import java.util.ArrayList

import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.view.View.GONE
import android.view.View.VISIBLE
import com.mapzen.erasermap.dummy.TestHelper.getFixture
import com.mapzen.erasermap.dummy.TestHelper.getTestFeature
import com.mapzen.erasermap.dummy.TestHelper.getTestLocation
import com.mapzen.erasermap.model.ManifestDownLoader
import com.squareup.okhttp.mockwebserver.MockResponse
import com.squareup.okhttp.mockwebserver.MockWebServer
import org.assertj.core.api.Assertions.assertThat
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowAlertDialog
import java.util.concurrent.TimeUnit

RunWith(PrivateMapsTestRunner::class)
Config(constants = BuildConfig::class,  sdk=intArrayOf(21))
public class MainActivityTest {
    private var activity: MainActivity? = null
    private var locationManager: LocationManager? = null
    private var shadowLocationManager: ShadowLocationManager? = null

    @Before
    throws(Exception::class)
    public fun setUp() {
        activity = Robolectric.setupActivity<MainActivity>(javaClass<MainActivity>())
        locationManager = activity!!.getSystemService(LOCATION_SERVICE) as LocationManager
        shadowLocationManager = shadowOf(locationManager)
    }

    @Test
    throws(Exception::class)
    public fun shouldNotBeNull() {
        assertThat(activity).isNotNull()
    }

    @Test
    throws(Exception::class)
    public fun shouldReturnAppName() {
        assertThat(activity!!.getString(R.string.app_name)).isEqualTo("Eraser Map")
    }

    @Test
    throws(Exception::class)
    public fun shouldHaveMapView() {
        assertThat(activity!!.findViewById(R.id.map)).isInstanceOf(javaClass<MapView>())
    }

    @Test
    throws(Exception::class)
    public fun shouldRequestLocationUpdates() {
        assertThat(
                shadowLocationManager!!.getRequestLocationUpdateListeners()).isNotEmpty()
    }

    @Test
    throws(Exception::class)
    public fun onPause_shouldDisconnectLocationServices() {
        activity!!.onPause()
        assertThat(LocationServices.FusedLocationApi).isNull()
        assertThat(
                shadowLocationManager!!.getRequestLocationUpdateListeners()).isEmpty()
    }

    @Test
    throws(Exception::class)
    public fun onResume_shouldReconnectLocationServices() {
        activity!!.onPause()
        (activity as MainActivity)?.onResume()
        assertThat(LocationServices.FusedLocationApi).isNotNull()
        assertThat(
                shadowLocationManager!!.getRequestLocationUpdateListeners()).isNotEmpty()
    }

    @Test
    throws(Exception::class)
    public fun shouldInjectLocationClient() {
        assertThat(activity!!.locationClient).isNotNull()
    }

    @Test
    throws(Exception::class)
    public fun shouldInjectSavedSearch() {
        assertThat(activity!!.savedSearch).isNotNull()
    }

    @Test
    throws(Exception::class)
    public fun onCreateOptionsMenu_shouldInflateOptionsMenu() {
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        assertThat(menu.findItem(R.id.action_search).getTitle()).isEqualTo("Search")
        assertThat(menu.findItem(R.id.action_clear).getTitle()).isEqualTo("Erase History")
        assertThat(menu.findItem(R.id.action_settings).getTitle()).isEqualTo("Settings")
    }

    @Test
    throws(Exception::class)
    public fun onOptionsItemSelected_shouldClearSavedSearchesOnActionClear() {
        activity!!.savedSearch!!.store("query")
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        activity!!.onOptionsItemSelected(menu.findItem(R.id.action_clear))
        assertThat(activity!!.savedSearch!!.size()).isEqualTo(0)
    }

    @Test
    throws(Exception::class)
    public fun onStop_shouldPersistSavedSearch() {
        activity!!.savedSearch!!.store("query")
        activity!!.onStop()
        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val serialized = prefs.getString(SavedSearch.TAG, null)

        val savedSearch = SavedSearch()
        savedSearch.deserialize(serialized)
        assertThat(savedSearch.get(0).getTerm()).isEqualTo("query")
    }

    @Test
    throws(Exception::class)
    public fun onCreate_shouldRestoreSavedSearch() {
        val savedSearch = SavedSearch()
        savedSearch.store("query")
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(SavedSearch.TAG,
                savedSearch.serialize()).commit()

        activity!!.onStart()
        assertThat(activity!!.savedSearch!!.get(0).getTerm()).isEqualTo("query")
    }

    @Test
    throws(Exception::class)
    public fun onDestroy_shouldSetCurrentSearchTerm() {
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_search).setActionView(PeliasSearchView(activity))
        menu.findItem(R.id.action_search).expandActionView()
        val searchView = menu.findItem(R.id.action_search).getActionView() as SearchView
        searchView.setQuery("query", false)
        searchView.requestFocus()
        activity?.onDestroy()
        assertThat(activity!!.presenter!!.currentSearchTerm).isEqualTo("query")
    }

    @Test
    throws(Exception::class)
    public fun onCreateOptionsMenu_shouldRestoreCurrentSearchTerm() {
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_search).setActionView(PeliasSearchView(activity))

        activity!!.presenter!!.currentSearchTerm = "query"
        activity!!.onCreateOptionsMenu(menu)
        val searchView = menu.findItem(R.id.action_search).getActionView() as SearchView
        assertThat(searchView.getQuery()).isEqualTo("query")
    }

    @Test
    throws(Exception::class)
    public fun showProgress_shouldSetProgressViewVisible() {
        activity!!.showProgress()
        assertThat(activity!!.findViewById(R.id.progress).getVisibility()).isEqualTo(VISIBLE)
    }

    @Test
    throws(Exception::class)
    public fun hideProgress_shouldSetProgressViewGone() {
        activity!!.showProgress()
        activity!!.hideProgress()
        assertThat(activity!!.findViewById(R.id.progress).getVisibility()).isEqualTo(GONE)
    }

    @Test
    throws(Exception::class)
    public fun showOverflowMenu_shouldShowOverflowGroup() {
        val menu = RoboMenuWithGroup(0, false)
        activity!!.onCreateOptionsMenu(menu)
        activity!!.showOverflowMenu()
        assertThat(menu.group).isEqualTo(R.id.menu_overflow)
        assertThat(menu.visible).isEqualTo(true)
    }

    @Test
    throws(Exception::class)
    public fun hideOverflowMenu_shouldHideOverflowGroup() {
        val menu = RoboMenuWithGroup(0, true)
        activity!!.onCreateOptionsMenu(menu)
        activity!!.hideOverflowMenu()
        assertThat(menu.group).isEqualTo(R.id.menu_overflow)
        assertThat(menu.visible).isEqualTo(false)
    }

    @Test
    throws(Exception::class)
    public fun showActionViewAll_shouldSetMenuItemVisible() {
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_view_all).setVisible(false)
        activity!!.showActionViewAll()
        assertThat(menu.findItem(R.id.action_view_all).isVisible()).isTrue()
    }

    @Test
    throws(Exception::class)
    public fun hideActionViewAll_shouldSetMenuItemNotVisible() {
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_view_all).setVisible(true)
        activity!!.hideActionViewAll()
        assertThat(menu.findItem(R.id.action_view_all).isVisible()).isFalse()
    }

    @Test
    throws(Exception::class)
    public fun showAllSearchResults_shouldStartSearchResultsActivityForResult() {
        activity!!.showAllSearchResults(ArrayList<Feature>())
        assertThat(shadowOf(
                activity).peekNextStartedActivityForResult().intent.getComponent().getClassName()).isEqualTo(
                javaClass<SearchResultsListActivity>().getName())
        assertThat(shadowOf(activity).peekNextStartedActivityForResult().requestCode).isEqualTo(
                activity!!.requestCodeSearchResults)
    }

    @Test
    throws(Exception::class)
    public fun showRoutePreview_shouldHideActionBar() {
        activity!!.getSupportActionBar()!!.show()
        activity!!.showRoutePreview(getTestFeature())
        activity!!.success(Route(JSONObject()))
        Robolectric.flushForegroundThreadScheduler()
        assertThat(activity!!.getSupportActionBar()!!.isShowing()).isFalse()
    }

    @Test
    throws(Exception::class)
    public fun showRoutePreview_shouldShowRoutePreviewView() {
        activity!!.findViewById(R.id.route_preview).setVisibility(GONE)
        activity!!.showRoutePreview(getTestFeature())
        activity!!.success(Route(JSONObject()))
        Robolectric.flushForegroundThreadScheduler()
        assertThat(activity!!.findViewById(R.id.route_preview).getVisibility()).isEqualTo(VISIBLE)
    }

    @Test
    public fun onRestoreViewState_shouldRestoreRoutingPreview() {
        activity!!.findViewById(R.id.route_preview).setVisibility(GONE)
        activity!!.showRoutePreview(getTestFeature())
        activity!!.success(Route(JSONObject()))
        Robolectric.flushForegroundThreadScheduler()
        activity!!.presenter!!.onRestoreViewState()
        assertThat(activity!!.findViewById(R.id.route_preview).getVisibility()).isEqualTo(VISIBLE)
    }

    @Test
    throws(Exception::class)
    public fun hideRoutePreview_shouldShowActionBar() {
        activity!!.getSupportActionBar()!!.hide()
        activity!!.hideRoutePreview()
        assertThat(activity!!.getSupportActionBar()!!.isShowing()).isTrue()
    }

    @Test
    throws(Exception::class)
    public fun hideRoutePreview_shouldHideRoutePreviewView() {
        activity!!.findViewById(R.id.route_preview).setVisibility(VISIBLE)
        activity!!.hideRoutePreview()
        assertThat(activity!!.findViewById(R.id.route_preview).getVisibility()).isEqualTo(GONE)
    }

    @Test
    throws(Exception::class)
    public fun onRadioClick_shouldChangeType() {
        activity!!.showRoutePreview(getTestFeature())
        activity!!.success(Route(getFixture("valhalla_route")))
        activity!!.findViewById(R.id.route_preview).findViewById(R.id.by_bike).performClick()
        assertThat(activity!!.type).isEqualTo(Router.Type.BIKING)
        activity!!.findViewById(R.id.route_preview).findViewById(R.id.by_foot).performClick()
        assertThat(activity!!.type).isEqualTo(Router.Type.WALKING)
        activity!!.findViewById(R.id.route_preview).findViewById(R.id.by_car).performClick()
        assertThat(activity!!.type).isEqualTo(Router.Type.DRIVING)
    }

    @Test
    throws(Exception::class)
    public fun onReverseClick_shouldSetReverse() {
        activity!!.showRoutePreview(getTestFeature())
        activity!!.success(Route(getFixture("valhalla_route")))
        assertThat(activity!!.reverse).isFalse()
        activity!!.findViewById(R.id.route_preview).findViewById(R.id.route_reverse).performClick()
        assertThat(activity!!.reverse).isTrue()
    }

    @Test
    throws(Exception::class)
    public fun onRoutingCircleClick_shouldOpenDirectionListActivity() {
        activity!!.reverse = true
        activity!!.showRoutePreview(getTestFeature())
        activity!!.success(Route(getFixture("valhalla_route")))
        activity!!.findViewById(R.id.routing_circle).performClick()
        val shadowActivity = shadowOf(activity)
        val startedIntent = shadowActivity.getNextStartedActivity()
        val shadowIntent = shadowOf(startedIntent)
        assertThat(shadowIntent.getComponent().getClassName()).contains("InstructionListActivity")
    }

    @Test
    throws(Exception::class)
    public fun showRoutingMode_shouldSetRoute() {
        activity!!.destination = getTestFeature()
        activity!!.success(Route(getFixture("valhalla_route")))
        activity!!.showRoutingMode(getTestFeature())
        val routeModeView = activity!!.findViewById(R.id.route_mode) as RouteModeView
        assertThat(routeModeView.route).isNotNull()
    }

    @Test
    throws(Exception::class)
    public fun hideRoutingMode_shouldClearRoute() {
        activity!!.showRoutePreview(getTestFeature())
        val routeModeView = activity!!.findViewById(R.id.route_mode) as RouteModeView
        routeModeView.route = Route(getFixture("valhalla_route"))
        activity!!.hideRoutingMode()
        assertThat(routeModeView.route).isNull()
    }

    @Test
    throws(Exception::class)
    public fun centerMapOnLocation_shouldSetCoordinates() {
        activity!!.centerMapOnLocation(getTestLocation(100.0, 200.0), 10f)
        assertThat(activity!!.mapController!!.getMapPosition()[0]).isEqualTo(100.0)
        assertThat(activity!!.mapController!!.getMapPosition()[1]).isEqualTo(200.0)
    }

    @Test
    throws(Exception::class)
    public fun centerMapOnLocation_resetSetZoom() {
        activity!!.mapController!!.setMapZoom(10f)
        activity!!.centerMapOnLocation(getTestLocation(100.0, 200.0), 10f)
        assertThat(activity!!.mapController!!.getMapZoom()).isEqualTo(10f)
    }

    @Test
    throws(Exception::class)
    public fun centerOnFeature_shouldSetCoordinates() {
        val featureList = ArrayList<Feature>()
        featureList.add(getTestFeature(34.0, 43.0))
        activity!!.centerOnCurrentFeature(featureList)
        Robolectric.flushForegroundThreadScheduler()
        assertThat(activity!!.mapController!!.getMapPosition()[0]).isEqualTo(43.0)
        assertThat(activity!!.mapController!!.getMapPosition()[1]).isEqualTo(34.0)
    }

    @Test
    throws(Exception::class)
    public fun showRoutingMode_shouldHideFindMeButton() {
        activity!!.destination = getTestFeature()
        activity!!.success(Route(getFixture("valhalla_route")))
        activity!!.showRoutingMode(getTestFeature())
        assertThat(activity!!.findViewById(R.id.find_me).getVisibility()).isEqualTo(View.GONE)
    }

    @Test
    throws(Exception::class)
    public fun hideRoutingMode_shouldShowFindMeButton() {
        activity!!.destination = getTestFeature()
        activity!!.findViewById(R.id.find_me).setVisibility(View.GONE)
        activity!!.hideRoutingMode()
        assertThat(activity!!.findViewById(R.id.find_me).getVisibility()).isEqualTo(View.VISIBLE)
    }

    @Test
    throws(Exception::class)
    public fun showReverseGeocodeFeature_shouldShowSearchPager() {
        val features = ArrayList<Feature>()
        features.add(getTestFeature(30.0, 30.0))
        activity!!.showReverseGeocodeFeature(features)
        assertThat(activity!!.findViewById(R.id.search_results).getVisibility()).isEqualTo(
                View.VISIBLE)
        assertThat(((activity!!.findViewById(R.id.search_results).findViewById(
                R.id.title)) as TextView).getText()).isEqualTo("Text")
    }

    @Test
    throws(Exception::class)
    public fun onLongClick_shouldSetPresenterFeature() {
        assertThat(activity!!.presenter!!.currentFeature).isNull()
        activity!!.reverseGeolocate(getLongPressMotionEvent())
        assertThat(activity!!.presenter!!.currentFeature).isNotNull()
    }

    @Test
    throws(Exception::class)
    public fun onMinVersionGreaterThanCurrent_shouldLaunchUpdateDialog() {
        var server: MockWebServer? = mockServerToMakeAppUpdate()
        activity?.checkIfUpdateNeeded()
        server?.shutdown()
        var dialog: AlertDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog.isShowing()).isTrue()
        assertThat(shadowOf(dialog).getMessage()).isEqualTo(activity?.getString(R.string.update_message))
    }

    @Test
    throws(Exception::class)
    public fun onMinVersionGreaterThanCurrent_clickUpdateNowShouldOpenPlayStore() {
        var server: MockWebServer? = mockServerToMakeAppUpdate()
        activity?.checkIfUpdateNeeded()
        server?.shutdown()
        var dialog: AlertDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog.isShowing()).isTrue()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        var startedIntent: Intent = shadowOf(activity).getNextStartedActivity();
        assertThat(startedIntent.getData().toString()).isEqualTo("market://details?id=com.mapzen.erasermap");
    }

    @Test
    throws(Exception::class)
    public fun onMinVersionGreaterThanCurrent_clickExitShouldExitApp() {
        var server: MockWebServer? = mockServerToMakeAppUpdate()
        activity?.checkIfUpdateNeeded()
        server?.shutdown()
        var dialog: AlertDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog.isShowing()).isTrue()
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
        assertThat(shadowOf(activity).isFinishing()).isTrue()
    }

    private fun mockServerToMakeAppUpdate(): MockWebServer? {
        var downLoader: ManifestDownLoader? = ManifestDownLoader()
        var server: MockWebServer? = MockWebServer()
        server?.play()
        downLoader?.host = server?.getUrl("/").toString()
        var sampleResponse: String = "{\"minVersion\": 101.0,\r\n" +
                "    \"vectorTileApiKeyReleaseProp\": \"vectorKey\",\r\n " +
                "   \"valhallaApiKey\": \"routeKey\",\r\n    " +
                "\"mintApiKey\": \"mintKey\",\r\n    " +
                "\"peliasApiKey\": \"peliasKey\"}\r\n"
        server?.enqueue(MockResponse().setBody(sampleResponse))
        downLoader?.download(activity?.apiKeys, {})
        server?.takeRequest(1000, TimeUnit.MILLISECONDS);
        return server
    }

    protected inner class RoboMenuWithGroup public constructor(public var group: Int, public var visible: Boolean) : RoboMenu() {

        override fun setGroupVisible(group: Int, visible: Boolean) {
            this.group = group
            this.visible = visible
        }
    }

    private fun getLongPressMotionEvent(): MotionEvent {
        return MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis() + 501,
                MotionEvent.ACTION_DOWN, 30.0f, 30.0f, 1.0f, 1.0f, 1, 1.0f, 1.0f, 0, 0)
    }
}
