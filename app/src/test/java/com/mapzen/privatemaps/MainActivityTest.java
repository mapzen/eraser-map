package com.mapzen.privatemaps;

import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.pelias.SavedSearch;

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
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.shadows.ShadowLocationManager;
import org.robolectric.util.ReflectionHelpers;

import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.preference.PreferenceManager;
import android.view.Menu;

import static android.content.Context.LOCATION_SERVICE;
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

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(MainActivity.class);
        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        shadowLocationManager = shadowOf(locationManager);
        mapView = (MapView) activity.findViewById(R.id.map);
        TestMap.TestAnimator.clearLastGeoPoint();
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
    public void shouldMarkerPositionOnLocationUpdate() throws Exception {
        LocationServices.FusedLocationApi.setMockMode(true);
        LocationServices.FusedLocationApi.setMockLocation(getTestLocation(1.0, 2.0));
        ItemizedLayer itemizedLayer = (ItemizedLayer) mapView.map().layers().get(4);
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
        assertThat(menu.findItem(R.id.action_clear).getTitle()).isEqualTo("Clear History");
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

    private Location getTestLocation(double lat, double lng) {
        Location location = new Location("test");
        location.setLatitude(lat);
        location.setLongitude(lng);
        return location;
    }
}
