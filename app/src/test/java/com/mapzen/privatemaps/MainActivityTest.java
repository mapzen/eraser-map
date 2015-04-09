package com.mapzen.privatemaps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.oscim.android.MapView;
import org.oscim.tiling.TileSource;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class)
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
        Mockito.verify(mapView.map()).setBaseMap(Mockito.any(TileSource.class));
    }
}
