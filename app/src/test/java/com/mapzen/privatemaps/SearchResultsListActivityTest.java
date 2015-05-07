package com.mapzen.privatemaps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import android.view.MenuItem;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class SearchResultsListActivityTest {
    private SearchResultsListActivity activity;

    @Before
    public void setUp() throws Exception {
        activity = Robolectric.setupActivity(SearchResultsListActivity.class);
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(activity).isNotNull();
    }

    @Test
    public void onOptionsItemSelected_shouldFinish() throws Exception {
        MenuItem menuItem = new RoboMenuItem(android.R.id.home);
        activity.onOptionsItemSelected(menuItem);
        assertThat(activity.isFinishing()).isTrue();
    }
}
