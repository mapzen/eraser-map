package com.mapzen.erasermap.route;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.R;
import com.mapzen.erasermap.TestHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.widget.TextView;

import static com.mapzen.erasermap.TestHelper.getTestSimpleFeature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class RoutePreviewViewTest {
    private RoutePreviewView routePreview;

    @Before
    public void setUp() throws Exception {
        routePreview = new RoutePreviewView(application);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(routePreview).isNotNull();
    }

    @Test
    public void setDestination_shouldPopulateTextView() throws Exception {
        routePreview.setDestination(getTestSimpleFeature());
        assertThat(((TextView) routePreview.findViewById(R.id.destination)).getText())
                .isEqualTo("Route from current location to " + TestHelper.TEST_TEXT);
    }
}
