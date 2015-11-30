package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.R;
import com.mapzen.erasermap.dummy.TestHelper;
import com.mapzen.pelias.SimpleFeature;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.util.ActivityController;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
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

    @Test
    public void shouldPopulateListViewWithLabel() throws Exception {
        ActivityController<SearchResultsListActivity> controller =
                Robolectric.buildActivity(SearchResultsListActivity.class);

        ArrayList<SimpleFeature> simpleFeatures = new ArrayList<>();
        SimpleFeature simpleFeature = TestHelper.getTestSimpleFeature();
        simpleFeatures.add(simpleFeature);
        Intent intent = new Intent();
        intent.putExtra("features", simpleFeatures);
        activity = controller.withIntent(intent).create().get();

        ListView listView = (ListView) activity.findViewById(R.id.search_results_list_view);
        TextView textView = (TextView) listView.getAdapter().getView(0, null, null);
        assertThat(textView.getText()).isEqualTo(simpleFeature.label());
    }
}
