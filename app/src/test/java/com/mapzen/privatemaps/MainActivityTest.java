package com.mapzen.privatemaps;

import com.mapzen.android.lost.api.LocationServices;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.android.MapView;
import org.oscim.layers.marker.ItemizedLayer;
import org.oscim.layers.tile.buildings.BuildingLayer;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.oscim.layers.tile.vector.labeling.LabelLayer;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.location.Location;

import static com.mapzen.privatemaps.TestMap.TestAnimator.getLastGeoPointAnimatedTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class MainActivityTest {
    private MainActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(MainActivity.class);
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
        MapView mapView = (MapView) activity.findViewById(R.id.map);
        assertThat(mapView.map().layers().get(1)).isInstanceOf(VectorTileLayer.class);
    }

    @Test
    public void shouldHaveBuildingLayer() throws Exception {
        MapView mapView = (MapView) activity.findViewById(R.id.map);
        assertThat(mapView.map().layers().get(2)).isInstanceOf(BuildingLayer.class);
    }

    @Test
    public void shouldHaveLabelLayer() throws Exception {
        MapView mapView = (MapView) activity.findViewById(R.id.map);
        assertThat(mapView.map().layers().get(3)).isInstanceOf(LabelLayer.class);
    }

    @Test
    public void shouldCenterOnLocationWhenFindMeButtonIsClicked() throws Exception {
        LocationServices.FusedLocationApi.setMockMode(true);
        LocationServices.FusedLocationApi.setMockLocation(getTestLocation(1.0, 2.0));
        activity.findViewById(R.id.find_me).performClick();
        assertThat(getLastGeoPointAnimatedTo().getLatitude()).isCloseTo(1.0, within(0.0001));
        assertThat(getLastGeoPointAnimatedTo().getLongitude()).isEqualTo(2.0, within(0.0001));
    }

    @Test
    public void shouldDisplayCurrentLocationIconWhenFindMeButtonIsClicked() throws Exception {
        LocationServices.FusedLocationApi.setMockMode(true);
        LocationServices.FusedLocationApi.setMockLocation(getTestLocation(1.0, 2.0));
        activity.findViewById(R.id.find_me).performClick();
        MapView mapView = (MapView) activity.findViewById(R.id.map);
        assertThat(mapView.map().layers().get(4)).isInstanceOf(ItemizedLayer.class);
    }

    private Location getTestLocation(double lat, double lng) {
        Location location = new Location("test");
        location.setLatitude(lat);
        location.setLongitude(lng);
        return location;
    }
}
