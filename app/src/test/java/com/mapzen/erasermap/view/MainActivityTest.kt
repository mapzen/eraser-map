package com.mapzen.erasermap.view

import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context.LOCATION_SERVICE
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.os.SystemClock
import android.preference.PreferenceManager
import android.support.v7.widget.SearchView
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import com.mapzen.android.lost.api.LocationServices
import com.mapzen.erasermap.BuildConfig
import com.mapzen.erasermap.PrivateMapsTestRunner
import com.mapzen.erasermap.R
import com.mapzen.erasermap.dummy.TestHelper.getFixture
import com.mapzen.erasermap.dummy.TestHelper.getTestFeature
import com.mapzen.erasermap.dummy.TestHelper.getTestLocation
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.widget.PeliasSearchView
import com.mapzen.tangram.MapView
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenu
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowAlertDialog
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLocationManager
import java.util.ArrayList
import java.util.concurrent.TimeUnit

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class MainActivityTest {
    private var activity: MainActivity? = null
    private var locationManager: LocationManager? = null
    private var shadowLocationManager: ShadowLocationManager? = null

    @Before
    public fun setUp() {
        activity = Robolectric.setupActivity<MainActivity>(javaClass<MainActivity>())
        locationManager = activity!!.getSystemService(LOCATION_SERVICE) as LocationManager
        shadowLocationManager = shadowOf(locationManager)
    }

    @Test
    public fun shouldNotBeNull() {
        assertThat(activity).isNotNull()
    }

    @Test
    public fun shouldReturnAppName() {
        assertThat(activity!!.getString(R.string.app_name)).isEqualTo("Eraser Map")
    }

    @Test
    public fun shouldHaveMapView() {
        assertThat(activity!!.findViewById(R.id.map)).isInstanceOf(javaClass<MapView>())
    }

    @Test
    public fun shouldRequestLocationUpdates() {
        assertThat(shadowLocationManager!!.getRequestLocationUpdateListeners()).isNotEmpty()
    }

    @Test
    public fun onPause_shouldDisconnectLocationServices() {
        activity!!.onPause()
        assertThat(LocationServices.FusedLocationApi).isNull()
        assertThat(shadowLocationManager!!.getRequestLocationUpdateListeners()).isEmpty()
    }

    @Test
    public fun onResume_shouldReconnectLocationServices() {
        activity!!.onPause()
        activity!!.onResume()
        assertThat(LocationServices.FusedLocationApi).isNotNull()
        assertThat(shadowLocationManager!!.getRequestLocationUpdateListeners()).isNotEmpty()
    }

    @Test
    public fun shouldInjectSavedSearch() {
        assertThat(activity!!.savedSearch).isNotNull()
    }

    @Test
    public fun onCreateOptionsMenu_shouldInflateOptionsMenu() {
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        assertThat(menu.findItem(R.id.action_search).getTitle()).isEqualTo("Search")
        assertThat(menu.findItem(R.id.action_clear).getTitle()).isEqualTo("Erase History")
        assertThat(menu.findItem(R.id.action_settings).getTitle()).isEqualTo("Settings")
    }

    @Test
    public fun onOptionsItemSelected_shouldClearSavedSearchesOnActionClear() {
        activity!!.savedSearch!!.store("query")
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        activity!!.onOptionsItemSelected(menu.findItem(R.id.action_clear))
        assertThat(activity!!.savedSearch!!.size()).isEqualTo(0)
    }

    @Test
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
    public fun onCreate_shouldRestoreSavedSearch() {
        val savedSearch = SavedSearch()
        savedSearch.store("query")
        PreferenceManager.getDefaultSharedPreferences(activity).edit().putString(SavedSearch.TAG,
                savedSearch.serialize()).commit()

        activity!!.onStart()
        assertThat(activity!!.savedSearch!!.get(0).getTerm()).isEqualTo("query")
    }

    @Test
    public fun onDestroy_shouldSetCurrentSearchTerm() {
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_search).setActionView(PeliasSearchView(activity))
        menu.findItem(R.id.action_search).expandActionView()
        val searchView = menu.findItem(R.id.action_search).getActionView() as SearchView
        searchView.setQuery("query", false)
        searchView.requestFocus()
        activity!!.onDestroy()
        assertThat(activity!!.presenter!!.currentSearchTerm).isEqualTo("query")
    }

    @Test
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
    public fun showProgress_shouldSetProgressViewVisible() {
        activity!!.showProgress()
        assertThat(activity!!.findViewById(R.id.progress).getVisibility()).isEqualTo(VISIBLE)
    }

    @Test
    public fun hideProgress_shouldSetProgressViewGone() {
        activity!!.showProgress()
        activity!!.hideProgress()
        assertThat(activity!!.findViewById(R.id.progress).getVisibility()).isEqualTo(GONE)
    }

    @Test
    public fun showOverflowMenu_shouldShowOverflowGroup() {
        val menu = RoboMenuWithGroup(0, false)
        activity!!.onCreateOptionsMenu(menu)
        activity!!.showOverflowMenu()
        assertThat(menu.group).isEqualTo(R.id.menu_overflow)
        assertThat(menu.visible).isEqualTo(true)
    }

    @Test
    public fun hideOverflowMenu_shouldHideOverflowGroup() {
        val menu = RoboMenuWithGroup(0, true)
        activity!!.onCreateOptionsMenu(menu)
        activity!!.hideOverflowMenu()
        assertThat(menu.group).isEqualTo(R.id.menu_overflow)
        assertThat(menu.visible).isEqualTo(false)
    }

    @Test
    public fun showActionViewAll_shouldSetMenuItemVisible() {
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_view_all).setVisible(false)
        activity!!.showActionViewAll()
        assertThat(menu.findItem(R.id.action_view_all).isVisible()).isTrue()
    }

    @Test
    public fun hideActionViewAll_shouldSetMenuItemNotVisible() {
        val menu = RoboMenu()
        activity!!.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_view_all).setVisible(true)
        activity!!.hideActionViewAll()
        assertThat(menu.findItem(R.id.action_view_all).isVisible()).isFalse()
    }

    @Test
    public fun showAllSearchResults_shouldStartSearchResultsActivityForResult() {
        activity!!.showAllSearchResults(ArrayList<Feature>())
        assertThat(shadowOf(activity).peekNextStartedActivityForResult().intent.getComponent()
                .getClassName()).isEqualTo(javaClass<SearchResultsListActivity>().getName())
        assertThat(shadowOf(activity).peekNextStartedActivityForResult().requestCode)
                .isEqualTo(activity!!.requestCodeSearchResults)
    }

    @Test
    public fun showRoutePreview_shouldHideActionBar() {
        activity!!.getSupportActionBar()!!.show()
        activity!!.showRoutePreview(getTestLocation(), getTestFeature())
        activity!!.success(Route(JSONObject()))
        Robolectric.flushForegroundThreadScheduler()
        assertThat(activity!!.getSupportActionBar()!!.isShowing()).isFalse()
    }

    @Test
    public fun showRoutePreview_shouldShowRoutePreviewView() {
        activity!!.findViewById(R.id.route_preview).setVisibility(GONE)
        activity!!.showRoutePreview(getTestLocation(), getTestFeature())
        activity!!.success(Route(JSONObject()))
        Robolectric.flushForegroundThreadScheduler()
        assertThat(activity!!.findViewById(R.id.route_preview).getVisibility()).isEqualTo(VISIBLE)
    }

    @Test
    public fun onRestoreViewState_shouldRestoreRoutingPreview() {
        activity!!.findViewById(R.id.route_preview).setVisibility(GONE)
        activity!!.showRoutePreview(getTestLocation(), getTestFeature())
        activity!!.success(Route(JSONObject()))
        Robolectric.flushForegroundThreadScheduler()
        activity!!.presenter!!.onRestoreViewState()
        assertThat(activity!!.findViewById(R.id.route_preview).getVisibility()).isEqualTo(VISIBLE)
    }

    @Test
    public fun hideRoutePreview_shouldShowActionBar() {
        activity!!.getSupportActionBar()!!.hide()
        activity!!.hideRoutePreview()
        assertThat(activity!!.getSupportActionBar()!!.isShowing()).isTrue()
    }

    @Test
    public fun hideRoutePreview_shouldHideRoutePreviewView() {
        activity!!.findViewById(R.id.route_preview).setVisibility(VISIBLE)
        activity!!.hideRoutePreview()
        assertThat(activity!!.findViewById(R.id.route_preview).getVisibility()).isEqualTo(GONE)
    }

    @Test
    public fun onRadioClick_shouldChangeType() {
        activity!!.showRoutePreview(getTestLocation(), getTestFeature())
        activity!!.success(Route(getFixture("valhalla_route")))
        activity!!.findViewById(R.id.route_preview).findViewById(R.id.by_bike).performClick()
        assertThat(activity!!.type).isEqualTo(Router.Type.BIKING)
        activity!!.findViewById(R.id.route_preview).findViewById(R.id.by_foot).performClick()
        assertThat(activity!!.type).isEqualTo(Router.Type.WALKING)
        activity!!.findViewById(R.id.route_preview).findViewById(R.id.by_car).performClick()
        assertThat(activity!!.type).isEqualTo(Router.Type.DRIVING)
    }

    @Test
    public fun onReverseClick_shouldSetReverse() {
        activity!!.showRoutePreview(getTestLocation(), getTestFeature())
        activity!!.success(Route(getFixture("valhalla_route")))
        assertThat(activity!!.reverse).isFalse()
        activity!!.findViewById(R.id.route_preview).findViewById(R.id.route_reverse).performClick()
        assertThat(activity!!.reverse).isTrue()
    }

    @Test
    public fun onRoutingCircleClick_shouldOpenDirectionListActivity() {
        activity!!.reverse = true
        activity!!.showRoutePreview(getTestLocation(), getTestFeature())
        activity!!.success(Route(getFixture("valhalla_route")))
        activity!!.findViewById(R.id.routing_circle).performClick()
        val shadowActivity = shadowOf(activity)
        val startedIntent = shadowActivity.getNextStartedActivity()
        val shadowIntent = shadowOf(startedIntent)
        assertThat(shadowIntent.getComponent().getClassName()).contains("InstructionListActivity")
    }

    @Test
    public fun showRoutingMode_shouldSetRoute() {
        activity!!.destination = getTestFeature()
        activity!!.success(Route(getFixture("valhalla_route")))
        activity!!.showRoutingMode(getTestFeature())
        val routeModeView = activity!!.findViewById(R.id.route_mode) as RouteModeView
        assertThat(routeModeView.route).isNotNull()
    }

    @Test
    public fun hideRoutingMode_shouldClearRoute() {
        activity!!.showRoutePreview(getTestLocation(), getTestFeature())
        val routeModeView = activity!!.findViewById(R.id.route_mode) as RouteModeView
        routeModeView.route = Route(getFixture("valhalla_route"))
        activity!!.hideRoutingMode()
        assertThat(routeModeView.route).isNull()
    }

    @Test
    public fun centerMapOnLocation_shouldSetCoordinates() {
        activity!!.centerMapOnLocation(getTestLocation(100.0, 200.0), 10f)
        assertThat(activity!!.mapController!!.getMapPosition()[0]).isEqualTo(100.0)
        assertThat(activity!!.mapController!!.getMapPosition()[1]).isEqualTo(200.0)
    }

    @Test
    public fun centerMapOnLocation_resetSetZoom() {
        activity!!.mapController!!.setMapZoom(10f)
        activity!!.centerMapOnLocation(getTestLocation(100.0, 200.0), 10f)
        assertThat(activity!!.mapController!!.getMapZoom()).isEqualTo(10f)
    }

    @Test
    public fun centerOnFeature_shouldSetCoordinates() {
        val featureList = ArrayList<Feature>()
        featureList.add(getTestFeature(34.0, 43.0))
        activity!!.centerOnCurrentFeature(featureList)
        Robolectric.flushForegroundThreadScheduler()
        assertThat(activity!!.mapController!!.getMapPosition()[0]).isEqualTo(43.0)
        assertThat(activity!!.mapController!!.getMapPosition()[1]).isEqualTo(34.0)
    }

    @Test
    public fun showRoutingMode_shouldHideFindMeButton() {
        activity!!.destination = getTestFeature()
        activity!!.success(Route(getFixture("valhalla_route")))
        activity!!.showRoutingMode(getTestFeature())
        assertThat(activity!!.findViewById(R.id.find_me).getVisibility()).isEqualTo(View.GONE)
    }

    @Test
    public fun hideRoutingMode_shouldShowFindMeButton() {
        activity!!.destination = getTestFeature()
        activity!!.findViewById(R.id.find_me).setVisibility(View.GONE)
        activity!!.hideRoutingMode()
        assertThat(activity!!.findViewById(R.id.find_me).getVisibility()).isEqualTo(View.VISIBLE)
    }

    @Test
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
    public fun onLongClick_shouldSetPresenterFeature() {
        assertThat(activity!!.presenter!!.currentFeature).isNull()
        activity!!.reverseGeolocate(getLongPressMotionEvent())
        assertThat(activity!!.presenter!!.currentFeature).isNotNull()
    }

    @Test
    public fun onMinVersionGreaterThanCurrent_shouldLaunchUpdateDialog() {
        activity!!.apiKeys?.setMinVersion(101)
        activity!!.checkIfUpdateNeeded()
        var dialog: AlertDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog.isShowing()).isTrue()
        assertThat(shadowOf(dialog).getMessage())
                .isEqualTo(activity!!.getString(R.string.update_message))
    }

    @Test
    public fun onMinVersionGreaterThanCurrent_clickUpdateNowShouldOpenPlayStore() {
        activity!!.apiKeys?.setMinVersion(101)
        activity!!.checkIfUpdateNeeded()
        var dialog: AlertDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog.isShowing()).isTrue()
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).performClick()
        var startedIntent: Intent = shadowOf(activity).getNextStartedActivity();
        assertThat(startedIntent.getData().toString())
                .isEqualTo("market://details?id=com.mapzen.erasermap");
    }

    @Test
    public fun onMinVersionGreaterThanCurrent_clickExitShouldExitApp() {
        activity!!.apiKeys?.setMinVersion(101)
        activity!!.checkIfUpdateNeeded()
        var dialog: AlertDialog = ShadowAlertDialog.getLatestAlertDialog()
        assertThat(dialog.isShowing()).isTrue()
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
        assertThat(shadowOf(activity).isFinishing()).isTrue()
    }

    @Test
    public fun onOptionsItemSelected_settingsShouldStartSettingsFragment() {
        val menuItem = RoboMenuItem(R.id.action_settings)
        activity!!.onOptionsItemSelected(menuItem)
        assertThat(ShadowApplication.getInstance().getNextStartedActivity().getComponent())
                .isEqualTo(ComponentName(activity, javaClass<SettingsActivity>()))
    }

    protected inner class RoboMenuWithGroup public constructor(public var group: Int,
            public var visible: Boolean) : RoboMenu() {

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
