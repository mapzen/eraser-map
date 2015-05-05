package com.mapzen.privatemaps;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;

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
    public void setAdapter_shouldSetPagerAdapter() throws Exception {
        PagerAdapter adapter = new TestPagerAdapter();
        searchResultsView.setAdapter(adapter);
        ViewPager pager = (ViewPager) searchResultsView.findViewById(R.id.pager);
        assertThat(pager.getAdapter()).isEqualTo(adapter);
    }

    private class TestPagerAdapter extends PagerAdapter {
        @Override public int getCount() {
            return 0;
        }

        @Override public boolean isViewFromObject(View view, Object object) {
            return false;
        }
    }
}
