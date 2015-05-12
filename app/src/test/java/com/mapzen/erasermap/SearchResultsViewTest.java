package com.mapzen.erasermap;

import com.mapzen.pelias.gson.Feature;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class SearchResultsViewTest {
    private SearchResultsView searchResultsView;

    @Before
    public void setUp() throws Exception {
        searchResultsView = new SearchResultsView(application, new TestAttributeSet());
    }

    @Test
    public void shouldNotBeNull() throws Exception {
        assertThat(searchResultsView).isNotNull();
    }

    @Test
    public void shouldInflatePager() throws Exception {
        assertThat(searchResultsView.findViewById(R.id.pager)).isNotNull();
    }

    @Test
    public void shouldInflateIndicator() throws Exception {
        assertThat(searchResultsView.findViewById(R.id.indicator)).isNotNull();
    }

    @Test
    public void setAdapter_shouldSetPagerAdapter() throws Exception {
        PagerAdapter adapter = new TestPagerAdapter();
        searchResultsView.setAdapter(adapter);
        ViewPager pager = (ViewPager) searchResultsView.findViewById(R.id.pager);
        assertThat(pager.getAdapter()).isEqualTo(adapter);
    }

    @Test
    public void setAdapter_shouldSetIndicatorText() throws Exception {
        PagerAdapter adapter = new TestPagerAdapter();
        searchResultsView.setAdapter(adapter);
        TextView indicator = (TextView) searchResultsView.findViewById(R.id.indicator);
        assertThat(indicator.getText()).isEqualTo("1 of 3 results");
    }

    @Test
    public void onPageSelected_shouldUpdateIndicatorText() throws Exception {
        PagerAdapter adapter = new TestPagerAdapter();
        searchResultsView.setAdapter(adapter);
        searchResultsView.onPageSelected(1);
        TextView indicator = (TextView) searchResultsView.findViewById(R.id.indicator);
        assertThat(indicator.getText()).isEqualTo("2 of 3 results");
    }

    @Test
    public void getCurrentItem_shouldReturnPagerCurrentItem() throws Exception {
        PagerAdapter adapter = new TestPagerAdapter();
        ViewPager pager = (ViewPager) searchResultsView.findViewById(R.id.pager);
        searchResultsView.setAdapter(adapter);
        pager.setCurrentItem(2);
        assertThat(searchResultsView.getCurrentItem()).isEqualTo(2);
    }

    @Test
    public void onPageSelected_shouldNotifyOnSearchResultSelectedListener() throws Exception {
        final ArrayList<Feature> features = new ArrayList<>();
        final Feature feature = SearchResultsAdapterTest.getTestFeature();
        features.add(feature);
        features.add(feature);
        features.add(feature);

        final SearchResultsAdapter adapter = new SearchResultsAdapter(application, features);
        final TestSelectedListener listener = new TestSelectedListener();
        searchResultsView.setAdapter(adapter);
        searchResultsView.setOnSearchResultsSelectedListener(listener);
        searchResultsView.onPageSelected(2);
        assertThat(listener.position).isEqualTo(2);
    }

    private class TestPagerAdapter extends PagerAdapter {
        @Override public int getCount() {
            return 3;
        }

        @Override public boolean isViewFromObject(View view, Object object) {
            return false;
        }
    }

    private class TestSelectedListener implements SearchResultsView.OnSearchResultSelectedListener {
        private int position;

        @Override public void onSearchResultSelected(int position) {
            this.position = position;
        }
    }
}
