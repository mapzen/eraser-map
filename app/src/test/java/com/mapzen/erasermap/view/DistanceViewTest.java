package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.dummy.TestAttributeSet;
import com.mapzen.helpers.DistanceFormatter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class DistanceViewTest {
    private DistanceView distanceView;

    @Before
    public void setUp() throws Exception {
        distanceView = new DistanceView(application, new TestAttributeSet());
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(distanceView).isNotNull();
    }

    @Test
    public void shouldGetDistance() throws Exception {
        distanceView.setDistanceInMeters(1000);
        assertThat(distanceView.getDistanceInMeters()).isEqualTo(1000);
    }

    @Test
    public void shouldFormatDistance() throws Exception {
        distanceView.setDistanceInMeters(1000);
        assertThat(distanceView.getText()).isEqualTo(DistanceFormatter.format(1000));
    }
}
