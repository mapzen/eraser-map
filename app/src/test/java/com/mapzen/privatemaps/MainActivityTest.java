package com.mapzen.privatemaps;

import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.pelias.SavedSearch;
import com.mapzen.pelias.gson.Feature;
import com.mapzen.pelias.widget.PeliasSearchView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.android.MapView;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.marker.MarkerItem;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.oscim.tiling.source.OkHttpEngine;
import org.oscim.tiling.source.oscimap4.OSciMap4TileSource;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.util.ReflectionHelpers;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.View;

import java.util.ArrayList;

import static android.content.Context.LOCATION_SERVICE;
import static com.mapzen.privatemaps.TestMap.TestAnimator;
import static com.mapzen.privatemaps.TestMap.TestAnimator.getLastGeoPoint;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.robolectric.Shadows.shadowOf;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class MainActivityTest {
    private MainActivity activity;
    private LocationManager locationManager;
    private ShadowLocationManager shadowLocationManager;
    private MapView mapView;
    private PrivateMapsApplication app;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(MainActivity.class);
        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
        mapView = (MapView) activity.findViewById(R.id.map);
        app = (PrivateMapsApplication) RuntimeEnvironment.application;
        TestAnimator.clearLastGeoPoint();
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void shouldReturnAppName() throws Exception {
        assertThat(activity.getString(R.string.app_name)).isEqualTo("Private Maps");
    }

    @Test
    public void shouldHaveMapView() throws Exception {
        assertThat(activity.findViewById(R.id.map)).isInstanceOf(MapView.class);
    }

    @Test
    public void shouldHaveBaseMap() throws Exception {
        assertThat(mapView.map().layers().get(1)).isInstanceOf(VectorTileLayer.class);
    }

    @Test
    public void shouldHaveBuildingLayer() throws Exception {
        assertThat(mapView.map().layers().get(2)).isInstanceOf(BuildingLayer.class);
    }

    @Test
    public void shouldHaveLabelLayer() throws Exception {
        assertThat(mapView.map().layers().get(3)).isInstanceOf(LabelLayer.class);
    }

    @Test
    public void shouldSetHttpEngine() throws Exception {
        VectorTileLayer baseLayer = activity.getMapController().getBaseLayer();
        OSciMap4TileSource tileSource = ReflectionHelpers.getField(baseLayer, "mTileSource");
        assertThat(tileSource.getHttpEngine()).isInstanceOf(OkHttpEngine.class);
    }

    @Test
    public void shouldCenterOnLocationWhenFindMeButtonIsClicked() throws Exception {
        LocationServices.FusedLocationApi.setMockMode(true);
        LocationServices.FusedLocationApi.setMockLocation(getTestLocation(1.0, 2.0));
        activity.findViewById(R.id.find_me).performClick();
        assertThat(getLastGeoPoint().getLatitude()).isCloseTo(1.0, within(0.0001));
        assertThat(getLastGeoPoint().getLongitude()).isEqualTo(2.0, within(0.0001));
    }

    @Test
    public void shouldDisplayCurrentLocationIconWhenFindMeButtonIsClicked() throws Exception {
        LocationServices.FusedLocationApi.setMockMode(true);
        LocationServices.FusedLocationApi.setMockLocation(getTestLocation(1.0, 2.0));
        activity.findViewById(R.id.find_me).performClick();
        assertThat(mapView.map().layers().get(4)).isInstanceOf(ItemizedLayer.class);
    }

    @Test
    public void shouldRequestLocationUpdates() throws Exception {
        assertThat(shadowLocationManager.getRequestLocationUpdateListeners()).isNotEmpty();
    }

    @Test
    public void shouldSetMarkerPositionOnLocationUpdate() throws Exception {
        LocationServices.FusedLocationApi.setMockMode(true);
        LocationServices.FusedLocationApi.setMockLocation(getTestLocation(1.0, 2.0));
        ItemizedLayer itemizedLayer = (ItemizedLayer) mapView.map().layers().get(5);
        MarkerItem item = itemizedLayer.removeItem(0);
        assertThat(item.geoPoint.getLatitude()).isEqualTo(1.0);
        assertThat(item.geoPoint.getLongitude()).isEqualTo(2.0);
    }

    @Test
    public void shouldUpdateMapOnLocationUpdate() throws Exception {
        LocationServices.FusedLocationApi.setMockMode(true);
        LocationServices.FusedLocationApi.setMockLocation(getTestLocation(1.0, 2.0));
        assertThat(((TestMap) mapView.map()).isUpdated()).isTrue();
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
    public void shouldInjectTileCache() throws Exception {
        assertThat(activity.getTileCache()).isNotNull();
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
        assertThat(activity.findViewById(R.id.progress).getVisibility()).isEqualTo(View.VISIBLE);
    }

    @Test
    public void hideProgress_shouldSetProgressViewGone() throws Exception {
        activity.showProgress();
        activity.hideProgress();
        assertThat(activity.findViewById(R.id.progress).getVisibility()).isEqualTo(View.GONE);
    }

    @Test
    public void showSearchResults_shouldAddFeaturesToPoiLayer() throws Exception {
        ArrayList<Feature> features = new ArrayList<>();
        features.add(SearchResultsAdapterTest.getTestFeature());
        features.add(SearchResultsAdapterTest.getTestFeature());
        features.add(SearchResultsAdapterTest.getTestFeature());
        activity.showSearchResults(features);
        assertThat(activity.getPoiLayer().size()).isEqualTo(3);
    }

    @Test
    public void hideSearchResults_shouldClearPoiLayer() throws Exception {
        ArrayList<Feature> features = new ArrayList<>();
        features.add(SearchResultsAdapterTest.getTestFeature());
        features.add(SearchResultsAdapterTest.getTestFeature());
        features.add(SearchResultsAdapterTest.getTestFeature());
        activity.showSearchResults(features);
        activity.hideSearchResults();
        assertThat(activity.getPoiLayer().size()).isEqualTo(0);
    }

    @Test
    public void showSearchResults_shouldCenterOnCurrentFeature() throws Exception {
        Feature feature = SearchResultsAdapterTest.getTestFeature(1.0, 2.0);
        ArrayList<Feature> features = new ArrayList<>();
        features.add(feature);
        activity.showSearchResults(features);
        Robolectric.flushForegroundScheduler();
        assertThat(TestAnimator.getLastGeoPoint().getLatitude()).isCloseTo(1.0, within(0.0001));
        assertThat(TestAnimator.getLastGeoPoint().getLongitude()).isEqualTo(2.0, within(0.0001));
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

    private Location getTestLocation(double lat, double lng) {
        Location location = new Location("test");
        location.setLatitude(lat);
        location.setLongitude(lng);
        return location;
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
}
