package com.mapzen.erasermap.view;

import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.R;
import com.mapzen.pelias.SavedSearch;
import com.mapzen.pelias.gson.Feature;
import com.mapzen.pelias.widget.PeliasSearchView;
import com.mapzen.tangram.MapView;
import com.mapzen.valhalla.Route;
import com.mapzen.valhalla.Router;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.shadows.ShadowMotionEvent;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.mapzen.erasermap.dummy.TestHelper.getFixture;
import static com.mapzen.erasermap.dummy.TestHelper.getTestFeature;
import static com.mapzen.erasermap.dummy.TestHelper.getTestLocation;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {
    private MainActivity activity;
    private LocationManager locationManager;
    private ShadowLocationManager shadowLocationManager;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(MainActivity.class);
        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void shouldReturnAppName() throws Exception {
        assertThat(activity.getString(R.string.app_name)).isEqualTo("Eraser Map");
    }

    @Test
    public void shouldHaveMapView() throws Exception {
        assertThat(activity.findViewById(R.id.map)).isInstanceOf(MapView.class);
    }

    @Test
    public void shouldRequestLocationUpdates() throws Exception {
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isNotEmpty();
    }

    @Test
    public void onPause_shouldDisconnectLocationServices() throws Exception {
        activity.onPause();
        assertThat(LocationServices.FusedLocationApi).isNull();
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isEmpty();
    }

    @Test
    public void onResume_shouldReconnectLocationServices() throws Exception {
        activity.onPause();
        activity.onResume();
        assertThat(LocationServices.FusedLocationApi).isNotNull();
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isNotEmpty();
    }

    @Test
    public void shouldInjectLocationClient() throws Exception {
        assertThat(activity.getLocationClient()).isNotNull();
    }

    @Test
    public void shouldInjectSavedSearch() throws Exception {
        assertThat(activity.getSavedSearch()).isNotNull();
    }

    @Test
    public void onCreateOptionsMenu_shouldInflateOptionsMenu() throws Exception {
        Menu menu = new RoboMenu();
        activity.onCreateOptionsMenu(menu);
        assertThat(menu.findItem(R.id.action_search).getTitle()).isEqualTo("Search");
        assertThat(menu.findItem(R.id.action_clear).getTitle()).isEqualTo("Erase History");
        assertThat(menu.findItem(R.id.action_settings).getTitle()).isEqualTo("Settings");
    }

    @Test
    public void onOptionsItemSelected_shouldClearSavedSearchesOnActionClear() throws Exception {
        activity.getSavedSearch().store("query");
        Menu menu = new RoboMenu();
        activity.onCreateOptionsMenu(menu);
        activity.onOptionsItemSelected(menu.findItem(R.id.action_clear));
        assertThat(activity.getSavedSearch().size()).isEqualTo(0);
    }

    @Test
    public void onStop_shouldPersistSavedSearch() throws Exception {
        activity.getSavedSearch().store("query");
        activity.onStop();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        String serialized = prefs.getString(SavedSearch.TAG, null);

        final SavedSearch savedSearch = new SavedSearch();
        savedSearch.deserialize(serialized);
        assertThat(savedSearch.get(0).getTerm()).isEqualTo("query");
    }

    @Test
    public void onCreate_shouldRestoreSavedSearch() throws Exception {
        final SavedSearch savedSearch = new SavedSearch();
        savedSearch.store("query");
        PreferenceManager.getDefaultSharedPreferences(activity)
                .edit()
                .putString(SavedSearch.TAG, savedSearch.serialize())
                .commit();

        activity.onStart();
        assertThat(activity.getSavedSearch().get(0).getTerm()).isEqualTo("query");
    }

    @Test
    public void onDestroy_shouldSetCurrentSearchTerm() throws Exception {
        Menu menu = new RoboMenu();
        activity.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_search).setActionView(new PeliasSearchView(activity));
        menu.findItem(R.id.action_search).expandActionView();
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQuery("query", false);
        searchView.requestFocus();
        activity.onDestroy();
        assertThat(activity.getPresenter().getCurrentSearchTerm()).isEqualTo("query");
    }

    @Test
    public void onCreateOptionsMenu_shouldRestoreCurrentSearchTerm() throws Exception {
        Menu menu = new RoboMenu();
        activity.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_search).setActionView(new PeliasSearchView(activity));

        activity.getPresenter().setCurrentSearchTerm("query");
        activity.onCreateOptionsMenu(menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        assertThat(searchView.getQuery()).isEqualTo("query");
    }

    @Test
    public void showProgress_shouldSetProgressViewVisible() throws Exception {
        activity.showProgress();
        assertThat(activity.findViewById(R.id.progress).getVisibility()).isEqualTo(VISIBLE);
    }

    @Test
    public void hideProgress_shouldSetProgressViewGone() throws Exception {
        activity.showProgress();
        activity.hideProgress();
        assertThat(activity.findViewById(R.id.progress).getVisibility()).isEqualTo(GONE);
    }

    @Test
    public void showOverflowMenu_shouldShowOverflowGroup() throws Exception {
        final RoboMenuWithGroup menu = new RoboMenuWithGroup(0, false);
        activity.onCreateOptionsMenu(menu);
        activity.showOverflowMenu();
        assertThat(menu.group).isEqualTo(R.id.menu_overflow);
        assertThat(menu.visible).isEqualTo(true);
    }

    @Test
    public void hideOverflowMenu_shouldHideOverflowGroup() throws Exception {
        final RoboMenuWithGroup menu = new RoboMenuWithGroup(0, true);
        activity.onCreateOptionsMenu(menu);
        activity.hideOverflowMenu();
        assertThat(menu.group).isEqualTo(R.id.menu_overflow);
        assertThat(menu.visible).isEqualTo(false);
    }

    @Test
    public void showActionViewAll_shouldSetMenuItemVisible() throws Exception {
        final RoboMenu menu = new RoboMenu();
        activity.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_view_all).setVisible(false);
        activity.showActionViewAll();
        assertThat(menu.findItem(R.id.action_view_all).isVisible()).isTrue();
    }

    @Test
    public void hideActionViewAll_shouldSetMenuItemNotVisible() throws Exception {
        final RoboMenu menu = new RoboMenu();
        activity.onCreateOptionsMenu(menu);
        menu.findItem(R.id.action_view_all).setVisible(true);
        activity.hideActionViewAll();
        assertThat(menu.findItem(R.id.action_view_all).isVisible()).isFalse();
    }

    @Test
    public void showAllSearchResults_shouldStartSearchResultsActivityForResult() throws Exception {
        activity.showAllSearchResults(new ArrayList<Feature>());
        assertThat(shadowOf(activity).peekNextStartedActivityForResult().intent.getComponent()
                .getClassName()).isEqualTo(SearchResultsListActivity.class.getName());
        assertThat(shadowOf(activity).peekNextStartedActivityForResult().requestCode)
                .isEqualTo(activity.getRequestCodeSearchResults());
    }

    @Test
    public void showRoutePreview_shouldHideActionBar() throws Exception {
        activity.getSupportActionBar().show();
        activity.showRoutePreview(getTestFeature());
        activity.success(new Route(new JSONObject()));
        Robolectric.flushForegroundThreadScheduler();
        assertThat(activity.getSupportActionBar().isShowing()).isFalse();
    }

    @Test
    public void showRoutePreview_shouldShowRoutePreviewView() throws Exception {
        activity.findViewById(R.id.route_preview).setVisibility(GONE);
        activity.showRoutePreview(getTestFeature());
        activity.success(new Route(new JSONObject()));
        Robolectric.flushForegroundThreadScheduler();
        assertThat(activity.findViewById(R.id.route_preview).getVisibility()).isEqualTo(VISIBLE);
    }

    @Test
    public void onRestoreViewState_shouldRestoreRoutingPreview() {
        activity.findViewById(R.id.route_preview).setVisibility(GONE);
        activity.showRoutePreview(getTestFeature());
        activity.success(new Route(new JSONObject()));
        Robolectric.flushForegroundThreadScheduler();
        activity.getPresenter().onRestoreViewState();
        assertThat(activity.findViewById(R.id.route_preview).getVisibility()).isEqualTo(VISIBLE);
    }

    @Test
    public void hideRoutePreview_shouldShowActionBar() throws Exception {
        activity.getSupportActionBar().hide();
        activity.hideRoutePreview();
        assertThat(activity.getSupportActionBar().isShowing()).isTrue();
    }

    @Test
    public void hideRoutePreview_shouldHideRoutePreviewView() throws Exception {
        activity.findViewById(R.id.route_preview).setVisibility(VISIBLE);
        activity.hideRoutePreview();
        assertThat(activity.findViewById(R.id.route_preview).getVisibility()).isEqualTo(GONE);
    }

    @Test
    public void onRadioClick_shouldChangeType() throws Exception {
        activity.showRoutePreview(getTestFeature());
        activity.success(new Route(getFixture("valhalla_route")));
        activity.findViewById(R.id.route_preview).findViewById(R.id.by_bike).performClick();
        assertThat(activity.getType()).isEqualTo(Router.Type.BIKING);
        activity.findViewById(R.id.route_preview).findViewById(R.id.by_foot).performClick();
        assertThat(activity.getType()).isEqualTo(Router.Type.WALKING);
        activity.findViewById(R.id.route_preview).findViewById(R.id.by_car).performClick();
        assertThat(activity.getType()).isEqualTo(Router.Type.DRIVING);
    }

    @Test
    public void onReverseClick_shouldSetReverse() throws Exception {
        activity.showRoutePreview(getTestFeature());
        activity.success(new Route(getFixture("valhalla_route")));
        assertThat(activity.getReverse()).isFalse();
        activity.findViewById(R.id.route_preview).findViewById(R.id.route_reverse).performClick();
        assertThat(activity.getReverse()).isTrue();
    }

    @Test
    public void onRoutingCircleClick_shouldOpenDirectionListActivity() throws Exception {
        activity.setReverse(true);
        activity.showRoutePreview(getTestFeature());
        activity.success(new Route(getFixture("valhalla_route")));
        activity.findViewById(R.id.routing_circle).performClick();
        ShadowActivity shadowActivity = shadowOf(activity);
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertThat(shadowIntent.getComponent().getClassName()).contains("InstructionListActivity");
    }

    @Test
    public void showRoutingMode_shouldSetRoute() throws Exception {
        activity.setDestination(getTestFeature());
        activity.success(new Route(getFixture("valhalla_route")));
        activity.showRoutingMode(getTestFeature());
        RouteModeView routeModeView = (RouteModeView) activity.findViewById(R.id.route_mode);
        assertThat(routeModeView.getRoute()).isNotNull();
    }

    @Test
    public void hideRoutingMode_shouldClearRoute() throws Exception {
        activity.showRoutePreview(getTestFeature());
        RouteModeView routeModeView = (RouteModeView) activity.findViewById(R.id.route_mode);
        routeModeView.setRoute(new Route(getFixture("valhalla_route")));
        activity.hideRoutingMode();
        assertThat(routeModeView.getRoute()).isNull();
    }

    @Test
    public void centerMapOnLocation_shouldSetCoordinates() throws Exception {
        activity.centerMapOnLocation(getTestLocation(100, 200), 10);
        assertThat(activity.getMapController().getMapPosition()[0]).isEqualTo(100);
        assertThat(activity.getMapController().getMapPosition()[1]).isEqualTo(200);
    }

    @Test
    public void centerMapOnLocation_resetSetZoom() throws Exception {
        activity.getMapController().setMapZoom(10);
        activity.centerMapOnLocation(getTestLocation(100, 200), 10);
        assertThat(activity.getMapController().getMapZoom()).isEqualTo(10);
    }

    @Test
    public void centerOnFeature_shouldSetCoordinates() throws Exception {
        ArrayList<Feature> featureList = new ArrayList<>();
        featureList.add(getTestFeature(34.0, 43.0));
        activity.centerOnCurrentFeature(featureList);
        Robolectric.flushForegroundThreadScheduler();
        assertThat(activity.getMapController().getMapPosition()[0]).isEqualTo(43.0);
        assertThat(activity.getMapController().getMapPosition()[1]).isEqualTo(34.0);
    }

    @Test
    public void showRoutingMode_shouldHideFindMeButton() throws Exception {
        activity.setDestination(getTestFeature());
        activity.success(new Route(getFixture("valhalla_route")));
        activity.showRoutingMode(getTestFeature());
        assertThat(activity.findViewById(R.id.find_me).getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void hideRoutingMode_shouldShowFindMeButton() throws Exception {
        activity.setDestination(getTestFeature());
        activity.findViewById(R.id.find_me).setVisibility(View.GONE);
        activity.hideRoutingMode();
        assertThat(activity.findViewById(R.id.find_me).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void showReverseGeocodeFeature_shouldShowSearchPager() throws Exception {
        ArrayList<Feature> features = new ArrayList<>();
        features.add(getTestFeature(30.0, 30.0));
        activity.showReverseGeocodeFeature(features);
        assertThat(activity.findViewById(R.id.search_results).getVisibility()).isEqualTo(
                View.VISIBLE);
        assertThat(((TextView) (activity.findViewById(R.id.search_results).findViewById(R.id.title))).getText()).isEqualTo(
                "Text");
    }

    @Test
    public void onLongClick_shouldSetPresenterFeature() throws Exception {
        assertThat(activity.getPresenter().getCurrentFeature()).isNull();
        activity.reverseGeolocate(getLongPressMotionEvent());
        assertThat(activity.getPresenter().getCurrentFeature()).isNotNull();
    }

    private class RoboMenuWithGroup extends RoboMenu {
        private int group;
        private boolean visible;

        private RoboMenuWithGroup(int group, boolean visible) {
            this.group = group;
            this.visible = visible;
        }

        @Override
        public void setGroupVisible(int group, boolean visible) {
            this.group = group;
            this.visible = visible;
        }
    }

    private MotionEvent getLongPressMotionEvent() {
        return MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis() + 501,
                MotionEvent.ACTION_DOWN,
                30.0f, 30.0f, 1.0f,1.0f, 1, 1.0f,1.0f,0, 0);
    }
}
