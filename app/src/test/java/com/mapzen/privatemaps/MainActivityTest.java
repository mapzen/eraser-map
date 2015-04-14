package com.mapzen.privatemaps;

import com.mapzen.android.lost.api.LocationServices;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oscim.android.MapView;
import org.oscim.layers.tile.vector.VectorTileLayer;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import android.location.Location;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void shouldCenterOnLocationWhenFindMeButtonIsClicked() throws Exception {
        Location location = new Location("test");
        location.setLatitude(1.0);
        location.setLongitude(2.0);
        LocationServices.FusedLocationApi.setMockMode(true);
        LocationServices.FusedLocationApi.setMockLocation(location);
        activity.findViewById(R.id.find_me).performClick();
        assertThat(TestMap.TestAnimator.getLastGeoPointAnimatedTo().getLatitude()).isEqualTo(1.0);
        assertThat(TestMap.TestAnimator.getLastGeoPointAnimatedTo().getLongitude()).isEqualTo(2.0);
    }
}
