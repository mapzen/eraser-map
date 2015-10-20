package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.dummy.TestHelper;
import com.mapzen.valhalla.Route;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static com.mapzen.erasermap.dummy.TestHelper.getTestSimpleFeature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RoutePreviewViewTest {
    private RoutePreviewView routePreview;
    private Route route;

    @Before
    public void setUp() throws Exception {
        route = mock(Route.class);
        when(route.getTotalDistance()).thenReturn(1609);
        when(route.getTotalTime()).thenReturn(5);
        routePreview = new RoutePreviewView(application);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(routePreview).isNotNull();
    }

    @Test
    public void setDestination_shouldPopulateTextView() throws Exception {
        routePreview.setDestination(getTestSimpleFeature());
        assertThat(routePreview.getDestinationView().getText()).isEqualTo(TestHelper.TEST_TEXT);
    }

    @Test
    public void setRoute_shouldPopulateCurrentLocation() throws Exception {
        routePreview.setRoute(route);
        assertThat(routePreview.getStartView().getText().toString()).isEqualTo("Current Location");
    }

    @Test
    public void setRoute_shouldPopulateDistancePreview() throws Exception {
        routePreview.setRoute(route);
        assertThat(routePreview.getDistancePreview().getText()).isEqualTo("1609");
    }

    @Test
    public void setRoute_shouldPopulateTimePreview() throws Exception {
        routePreview.setRoute(route);
        assertThat(routePreview.getTimePreview().getText()).isEqualTo("5");
    }
}
