package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.valhalla.Route;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static com.mapzen.erasermap.dummy.TestHelper.TEST_NAME;
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
        when(route.getTotalTime()).thenReturn(300);
        routePreview = new RoutePreviewView(application);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(routePreview).isNotNull();
    }

    @Test
    public void setDestination_shouldPopulateTextView() throws Exception {
        routePreview.setDestination(getTestSimpleFeature());
        assertThat(routePreview.getDestinationView().getText()).isEqualTo(TEST_NAME);
    }

    @Test
    public void setDestination_shouldPopulateCurrentLocation() throws Exception {
        routePreview.setDestination(getTestSimpleFeature());
        assertThat(routePreview.getStartView().getText()).isEqualTo("Current Location");
    }

    @Test
    public void setRoute_shouldPopulateDistancePreview() throws Exception {
        routePreview.setRoute(route);
        assertThat(routePreview.getDistancePreview().getText()).isEqualTo("1 mi");
    }

    @Test
    public void setRoute_shouldPopulateTimePreview() throws Exception {
        routePreview.setRoute(route);
        assertThat(routePreview.getTimePreview().getText()).isEqualTo("5 mins");
    }

    @Test
    public void reverse_shouldSwapStartAndDestination() throws Exception {
        routePreview.setDestination(getTestSimpleFeature());
        routePreview.setRoute(route);
        routePreview.setReverse(true);
        assertThat(routePreview.getStartView().getText().toString()).isEqualTo(TEST_NAME);
        assertThat(routePreview.getDestinationView().getText()).isEqualTo("Current Location");
    }
}
