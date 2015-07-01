package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.R;
import com.mapzen.erasermap.dummy.TestHelper;
import com.mapzen.valhalla.Route;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.widget.TextView;

import static com.mapzen.erasermap.dummy.TestHelper.getTestSimpleFeature;
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
                .isEqualTo(TestHelper.TEST_TEXT);
    }

    @Test
    public void onStart_shouldHaveCurrentLocation() throws Exception {
        routePreview.setRoute(new Route(new JSONObject()));
        TextView textView = (TextView) routePreview.findViewById(R.id.starting_point);
        assertThat(textView).isNotNull();
        assertThat(textView.getText().toString()).isEqualTo("Current Location");
    }
}
