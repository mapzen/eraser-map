package com.mapzen.erasermap.view

import android.content.ComponentName
import android.content.Context.LOCATION_SERVICE
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
import com.mapzen.erasermap.dummy.TestHelper.*
import com.mapzen.erasermap.presenter.MainPresenter
import com.mapzen.erasermap.shadows.ShadowMapData
import com.mapzen.pelias.SavedSearch
import com.mapzen.pelias.gson.Feature
import com.mapzen.pelias.widget.PeliasSearchView
import com.mapzen.tangram.LngLat
import com.mapzen.tangram.MapData
import com.mapzen.tangram.MapView
import com.mapzen.valhalla.Route
import com.mapzen.valhalla.Router
import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.fakes.RoboMenu
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.internal.ShadowExtractor
import org.robolectric.shadows.ShadowApplication
import java.util.ArrayList

@RunWith(PrivateMapsTestRunner::class)
@Config(constants = BuildConfig::class, sdk=intArrayOf(21))
public class MainActivityTest {
    val activity = Robolectric.setupActivity<MainActivity>(javaClass<MainActivity>())
    val locationManager = activity.getSystemService(LOCATION_SERVICE) as LocationManager
    val shadowLocationManager = shadowOf(locationManager)

    @Test
    public fun shouldNotBeNull() {
        assertThat(activity).isNotNull()
    }

    @Test
    public fun shouldReturnAppName() {
        assertThat(activity.getString(R.string.app_name)).isEqualTo("Eraser Map")
    }

    @Test
    public fun shouldHaveMapView() {
        assertThat(activity.findViewById(R.id.map)).isInstanceOf(javaClass<MapView>())
    }

    @Test
    public fun onPause_shouldDisconnectLocationServices() {
        activity.onPause()
        assertThat(LocationServices.FusedLocationApi).isNull()
        assertThat(shadowLocationManager!!.getRequestLocationUpdateListeners()).isEmpty()
    }

    @Test
    public fun onResume_shouldReconnectLocationServices() {
        activity.onPause()
        activity.onResume()
        assertThat(LocationServices.FusedLocationApi).isNotNull()
        assertThat(shadowLocationManager!!.getRequestLocationUpdateListeners()).isNotEmpty()
    }

    @Test
    public fun shouldInjectSavedSearch() {
        assertThat(activity.savedSearch).isNotNull()
    }

    @Test
    public fun onCreateOptionsMenu_shouldInflateOptionsMenu() {
        val menu = RoboMenu()
        activity.onCreateOptionsMenu(menu)
        assertThat(menu.findItem(R.id.action_search).getTitle()).isEqualTo("Search")
        assertThat(menu.findItem(R.id.action_clear).getTitle()).isEqualTo("Erase History")
        assertThat(menu.findItem(R.id.action_settings).getTitle()).isEqualTo("Settings")
    }

    @Test
    public fun onOptionsItemSelected_shouldClearSavedSearchesOnActionClear() {
        activity.savedSearch!!.store("query")
        val menu = RoboMenu()
        activity.onCreateOptionsMenu(menu)
        activity.onOptionsItemSelected(menu.findItem(R.id.action_clear))
        assertThat(activity.savedSearch!!.size()).isEqualTo(0)
    }

    @Test
    public fun onStop_shouldPersistSavedSearch() {
        activity.savedSearch!!.store("query")
        activity.onStop()
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

        activity.onStart()
        assertThat(activity.savedSearch!!.get(0).getTerm()).isEqualTo("query")
    }

    @Test
    public fun onDestroy_shouldSetCurrentSearchTerm() {
        val menu = RoboMenu()
        activity.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_search).setActionView(PeliasSearchView(activity))
        menu.findItem(R.id.action_search).expandActionView()
        val searchView = menu.findItem(R.id.action_search).getActionView() as SearchView
        searchView.setQuery("query", false)
        searchView.requestFocus()
        activity.onDestroy()
        assertThat(activity.presenter!!.currentSearchTerm).isEqualTo("query")
    }

    @Test
    public fun onCreateOptionsMenu_shouldRestoreCurrentSearchTerm() {
        val menu = RoboMenu()
        activity.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_search).setActionView(PeliasSearchView(activity))

        activity.presenter!!.currentSearchTerm = "query"
        activity.onCreateOptionsMenu(menu)
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView
        assertThat(searchView.query).isEqualTo("query")
    }

    @Test
    public fun showProgress_shouldSetProgressViewVisible() {
        activity.showProgress()
        assertThat(activity.findViewById(R.id.progress).visibility).isEqualTo(VISIBLE)
    }

    @Test
    public fun hideProgress_shouldSetProgressViewGone() {
        activity.showProgress()
        activity.hideProgress()
        assertThat(activity.findViewById(R.id.progress).visibility).isEqualTo(GONE)
    }

    @Test
    public fun showOverflowMenu_shouldShowOverflowGroup() {
        val menu = RoboMenuWithGroup(0, false)
        activity.onCreateOptionsMenu(menu)
        activity.showOverflowMenu()
        assertThat(menu.group).isEqualTo(R.id.menu_overflow)
        assertThat(menu.visible).isEqualTo(true)
    }

    @Test
    public fun hideOverflowMenu_shouldHideOverflowGroup() {
        val menu = RoboMenuWithGroup(0, true)
        activity.onCreateOptionsMenu(menu)
        activity.hideOverflowMenu()
        assertThat(menu.group).isEqualTo(R.id.menu_overflow)
        assertThat(menu.visible).isEqualTo(false)
    }

    @Test
    public fun showActionViewAll_shouldSetMenuItemVisible() {
        val menu = RoboMenu()
        activity.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_view_all).setVisible(false)
        activity.showActionViewAll()
        assertThat(menu.findItem(R.id.action_view_all).isVisible).isTrue()
    }

    @Test
    public fun hideActionViewAll_shouldSetMenuItemNotVisible() {
        val menu = RoboMenu()
        activity.onCreateOptionsMenu(menu)
        menu.findItem(R.id.action_view_all).setVisible(true)
        activity.hideActionViewAll()
        assertThat(menu.findItem(R.id.action_view_all).isVisible).isFalse()
    }

    @Test
    public fun showAllSearchResults_shouldStartSearchResultsActivityForResult() {
        activity.showAllSearchResults(ArrayList<Feature>())
        assertThat(shadowOf(activity).peekNextStartedActivityForResult().intent.component
                .className).isEqualTo(SearchResultsListActivity::class.java.name)
        assertThat(shadowOf(activity).peekNextStartedActivityForResult().requestCode)
                .isEqualTo(activity.requestCodeSearchResults)
    }

    @Test
    public fun showRoutePreview_shouldHideActionBar() {
        activity.supportActionBar!!.show()
        activity.showRoutePreview(getTestLocation(), getTestFeature())
        activity.success(TestRoute())
        Robolectric.flushForegroundThreadScheduler()
        assertThat(activity.supportActionBar!!.isShowing).isFalse()
    }

    @Test
    public fun showRoutePreview_shouldShowRoutePreviewView() {
        activity.findViewById(R.id.route_preview).setVisibility(GONE)
        activity.showRoutePreview(getTestLocation(), getTestFeature())
        activity.success(TestRoute())
        Robolectric.flushForegroundThreadScheduler()
        assertThat(activity.findViewById(R.id.route_preview).visibility).isEqualTo(VISIBLE)
    }

    @Test
    public fun showRoutePreview_shouldDrawRouteLine() {
        activity.showRoutePreview(getTestLocation(), getTestFeature())
        activity.success(TestRoute())
        Robolectric.flushForegroundThreadScheduler()
        assertThat((ShadowExtractor.extract(activity.routeLine) as ShadowMapData).line).isNotNull()
    }

    @Test
    public fun showRoutePreview_shouldClearPreviousRouteLine() {
        val properties = com.mapzen.tangram.Properties()
        properties.add("type", "line");
        val routeLine = ArrayList<LngLat>()
        routeLine.add(LngLat())
        activity.routeLine = MapData("route")
        activity.routeLine?.addLine(properties, routeLine)
        activity.showRoutePreview(getTestLocation(), getTestFeature())
        activity.success(TestRoute())
        Robolectric.flushForegroundThreadScheduler()
        assertThat((ShadowExtractor.extract(activity.routeLine) as ShadowMapData).line)
                .isNotSameAs(routeLine)
    }

    @Test
    public fun centerOnMapLocation_shouldAddPointToMap() {
        activity.centerMapOnLocation(getTestLocation(), MainPresenter.DEFAULT_ZOOM)
        val shadowFindMe = ShadowExtractor.extract(activity.findMe) as ShadowMapData
        assertThat(shadowFindMe.points).isNotNull()
    }

    @Test
    public fun centerOnMapLocation_shouldClearPreviousPoint() {
        activity.centerMapOnLocation(getTestLocation(), MainPresenter.DEFAULT_ZOOM)
        activity.centerMapOnLocation(getTestLocation(), MainPresenter.DEFAULT_ZOOM)
        val shadowFindMe = ShadowExtractor.extract(activity.findMe) as ShadowMapData
        assertThat(shadowFindMe.points.size).isEqualTo(1)
    }

    @Test
    public fun showSearchResults_shouldAddPointsToMap() {
        val features = ArrayList<Feature>()
        features.add(getTestFeature())
        features.add(getTestFeature())
        features.add(getTestFeature())
        activity.showSearchResults(features)
        val shadowSearchResults = ShadowExtractor.extract(activity.searchResults) as ShadowMapData
        assertThat(shadowSearchResults.points).hasSize(3)
    }

    @Test
    public fun showSearchResults_shouldClearPreviousPoints() {
        val features = ArrayList<Feature>()
        features.add(getTestFeature())
        features.add(getTestFeature())
        features.add(getTestFeature())
        activity.showSearchResults(features)
        activity.showSearchResults(features)
        val shadowSearchResults = ShadowExtractor.extract(activity.searchResults) as ShadowMapData
        assertThat(shadowSearchResults.points).hasSize(3)
    }

    @Test
    public fun onRestoreViewState_shouldRestoreRoutingPreview() {
        activity.findViewById(R.id.route_preview).visibility = GONE
        activity.showRoutePreview(getTestLocation(), getTestFeature())
        activity.success(TestRoute())
        Robolectric.flushForegroundThreadScheduler()
        activity.presenter!!.onRestoreViewState()
        assertThat(activity.findViewById(R.id.route_preview).visibility).isEqualTo(VISIBLE)
    }

    @Test
    public fun hideRoutePreview_shouldShowActionBar() {
        activity.getSupportActionBar()!!.hide()
        activity.hideRoutePreview()
        assertThat(activity.supportActionBar!!.isShowing).isTrue()
    }

    @Test
    public fun hideRoutePreview_shouldHideRoutePreviewView() {
        activity.findViewById(R.id.route_preview).setVisibility(VISIBLE)
        activity.hideRoutePreview()
        assertThat(activity.findViewById(R.id.route_preview).getVisibility()).isEqualTo(GONE)
    }

    @Test
    public fun onRadioClick_shouldChangeType() {
        activity.showRoutePreview(getTestLocation(), getTestFeature())
        activity.success(Route(getFixture("valhalla_route")))
        activity.findViewById(R.id.route_preview).findViewById(R.id.by_bike).performClick()
        assertThat(activity.routeManager?.type).isEqualTo(Router.Type.BIKING)
        activity.findViewById(R.id.route_preview).findViewById(R.id.by_foot).performClick()
        assertThat(activity.routeManager?.type).isEqualTo(Router.Type.WALKING)
        activity.findViewById(R.id.route_preview).findViewById(R.id.by_car).performClick()
        assertThat(activity.routeManager?.type).isEqualTo(Router.Type.DRIVING)
    }

    @Test
    public fun onReverseClick_shouldSetReverse() {
        activity.showRoutePreview(getTestLocation(), getTestFeature())
        activity.success(Route(getFixture("valhalla_route")))
        assertThat(activity.routeManager?.reverse).isFalse()
        activity.findViewById(R.id.route_preview).findViewById(R.id.route_reverse).performClick()
        assertThat(activity.routeManager?.reverse).isTrue()
    }

    @Test
    public fun onViewListButtonClick_shouldOpenDirectionListActivity() {
        activity.showRoutePreview(getTestLocation(), getTestFeature())
        activity.success(TestRoute())
        activity.findViewById(R.id.view_list).performClick()
        val shadowActivity = shadowOf(activity)
        val startedIntent = shadowActivity.nextStartedActivity
        val shadowIntent = shadowOf(startedIntent)
        assertThat(shadowIntent.component.className).contains("InstructionListActivity")
    }

    @Test
    public fun startRoutingMode_shouldSetRoute() {
        activity.routeManager?.destination = getTestFeature()
        activity.success(Route(getFixture("valhalla_route")))
        activity.startRoutingMode(getTestFeature())
        val routeModeView = activity.findViewById(R.id.route_mode) as RouteModeView
        assertThat(routeModeView.route).isNotNull()
    }

    @Test
    public fun hideRoutingMode_shouldClearRoute() {
        activity.showRoutePreview(getTestLocation(), getTestFeature())
        val routeModeView = activity.findViewById(R.id.route_mode) as RouteModeView
        routeModeView.route = Route(getFixture("valhalla_route"))
        activity.hideRoutingMode()
        assertThat(routeModeView.route).isNull()
    }

    @Test
    public fun centerMapOnLocation_shouldSetCoordinates() {
        activity.centerMapOnLocation(getTestLocation(100.0, 200.0), 10f)
        assertThat(activity.mapController!!.getMapPosition().longitude).isEqualTo(100.0)
        assertThat(activity.mapController!!.getMapPosition().latitude).isEqualTo(200.0)
    }

    @Test
    public fun centerMapOnLocation_resetSetZoom() {
        activity.mapController!!.setMapZoom(10f)
        activity.centerMapOnLocation(getTestLocation(100.0, 200.0), 10f)
        assertThat(activity.mapController!!.getMapZoom()).isEqualTo(10f)
    }

    @Test
    public fun centerOnFeature_shouldSetCoordinates() {
        val featureList = ArrayList<Feature>()
        featureList.add(getTestFeature(34.0, 43.0))
        activity.centerOnCurrentFeature(featureList)
        Robolectric.flushForegroundThreadScheduler()
        assertThat(activity.mapController!!.getMapPosition().longitude).isEqualTo(43.0)
        assertThat(activity.mapController!!.getMapPosition().latitude).isEqualTo(34.0)
    }

    @Test
    public fun startRoutingMode_shouldHideFindMeButton() {
        activity.routeManager?.destination = getTestFeature()
        activity.success(Route(getFixture("valhalla_route")))
        activity.startRoutingMode(getTestFeature())
        assertThat(activity.findViewById(R.id.find_me).getVisibility()).isEqualTo(View.GONE)
    }

    @Test
    public fun hideRoutingMode_shouldShowFindMeButton() {
        activity.routeManager?.destination = getTestFeature()
        activity.findViewById(R.id.find_me).setVisibility(View.GONE)
        activity.hideRoutingMode()
        assertThat(activity.findViewById(R.id.find_me).getVisibility()).isEqualTo(View.VISIBLE)
    }

    @Test
    public fun showReverseGeocodeFeature_shouldShowSearchPager() {
        val features = ArrayList<Feature>()
        features.add(getTestFeature(30.0, 30.0))
        activity.showReverseGeocodeFeature(features)
        assertThat(activity.findViewById(R.id.search_results).getVisibility()).isEqualTo(
                View.VISIBLE)
        assertThat(((activity.findViewById(R.id.search_results).findViewById(R.id.title))
                as TextView).getText()).isEqualTo("Text")
    }

    @Test
    public fun onLongClick_shouldSetPresenterFeature() {
        assertThat(activity.presenter!!.currentFeature).isNull()
        activity.reverseGeolocate(getLongPressMotionEvent())
        assertThat(activity.presenter!!.currentFeature).isNotNull()
    }

    @Test
    public fun onOptionsItemSelected_settingsShouldStartSettingsFragment() {
        val menuItem = RoboMenuItem(R.id.action_settings)
        activity.onOptionsItemSelected(menuItem)
        assertThat(ShadowApplication.getInstance().nextStartedActivity.component)
                .isEqualTo(ComponentName(activity, SettingsActivity::class.java))
    }

    @Test
    public fun startRoutingMode_shouldHideFindMeIcon() {
        activity.showCurrentLocation(getTestLocation())
        val shadowMapData = ShadowExtractor.extract(activity.findMe) as ShadowMapData
        activity.success(TestRoute())
        activity.startRoutingMode(getTestFeature())
        assertThat(shadowMapData.points).isNullOrEmpty()
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

    private class TestRoute : Route(JSONObject()) {
        override fun getTotalDistance(): Int {
            return 0;
        }

        override fun getTotalTime(): Int {
            return 0;
        }
    }
}
