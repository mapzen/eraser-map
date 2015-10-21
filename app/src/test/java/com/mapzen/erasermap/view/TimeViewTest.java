package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.dummy.TestAttributeSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TimeViewTest {
    private TimeView timeView;

    @Before public void setUp() throws Exception {
        timeView = new TimeView(application, new TestAttributeSet());
    }

    @Test public void shouldNotBeNull() throws Exception {
        assertThat(timeView).isNotNull();
    }

    @Test public void shouldFormatLessThanOneMinute() throws Exception {
        timeView.setTimeInMinutes(0);
        assertThat(timeView.getText()).isEqualTo("<1 min");
    }

    @Test public void shouldFormatOneMinute() throws Exception {
        timeView.setTimeInMinutes(1);
        assertThat(timeView.getText()).isEqualTo("1 min");
    }

    @Test public void shouldFormatLessThanSixtyMinutes() throws Exception {
        timeView.setTimeInMinutes(2);
        assertThat(timeView.getText()).isEqualTo("2 mins");

        timeView.setTimeInMinutes(59);
        assertThat(timeView.getText()).isEqualTo("59 mins");
    }

    @Test public void shouldFormatOneHour() throws Exception {
        timeView.setTimeInMinutes(60);
        assertThat(timeView.getText()).isEqualTo("1 hr");
    }

    @Test public void shouldFormatOneHourPlusOneMinute() throws Exception {
        timeView.setTimeInMinutes(61);
        assertThat(timeView.getText()).isEqualTo("1 hr 1 min");
    }

    @Test public void shouldFormatOneHourPlusMinutes() throws Exception {
        timeView.setTimeInMinutes(62);
        assertThat(timeView.getText()).isEqualTo("1 hr 2 mins");

        timeView.setTimeInMinutes(119);
        assertThat(timeView.getText()).isEqualTo("1 hr 59 mins");
    }

    @Test public void shouldFormatMoreThanOneHour() throws Exception {
        timeView.setTimeInMinutes(120);
        assertThat(timeView.getText()).isEqualTo("2 hrs");

        timeView.setTimeInMinutes(180);
        assertThat(timeView.getText()).isEqualTo("3 hrs");
    }

    @Test public void shouldFormatMoreThanOneHourPlusOneMinute() throws Exception {
        timeView.setTimeInMinutes(121);
        assertThat(timeView.getText()).isEqualTo("2 hrs 1 min");

        timeView.setTimeInMinutes(181);
        assertThat(timeView.getText()).isEqualTo("3 hrs 1 min");
    }

    @Test public void shouldFormatMoreThanOneHourPlusMoreThanOneMinute() throws Exception {
        timeView.setTimeInMinutes(122);
        assertThat(timeView.getText()).isEqualTo("2 hrs 2 mins");

        timeView.setTimeInMinutes(179);
        assertThat(timeView.getText()).isEqualTo("2 hrs 59 mins");
    }
}
