package com.mapzen.erasermap.view;

import com.mapzen.erasermap.BuildConfig;
import com.mapzen.erasermap.PrivateMapsTestRunner;
import com.mapzen.erasermap.R;
import com.mapzen.erasermap.model.ConfidenceHandler;
import com.mapzen.erasermap.model.event.RoutePreviewEvent;
import com.mapzen.erasermap.presenter.TestMainPresenterImpl;
import com.mapzen.pelias.SimpleFeature;
import com.mapzen.pelias.gson.Feature;

import com.squareup.otto.Subscribe;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import static com.mapzen.erasermap.dummy.TestHelper.getTestFeature;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class SearchResultsAdapterTest {
    private SearchResultsAdapter adapter;
    private Feature feature;

    @Before
    public void setUp() throws Exception {
        feature = getTestFeature();
        ArrayList<Feature> features = new ArrayList<>();
        features.add(feature);
        adapter = new SearchResultsAdapter(application, features,
                new ConfidenceHandler(new TestMainPresenterImpl()));
    }

    @Test
    public void instantiateItem_shouldPopulateTitle() throws Exception {
        ViewGroup container = new FrameLayout(application);
        View view = (View) adapter.instantiateItem(container, 0);
        TextView text = (TextView) view.findViewById(R.id.title);
        assertThat(text.getText()).isEqualTo(SimpleFeature.fromFeature(feature).name());
    }

    @Test
    public void instantiateItem_shouldPopulateAddress() throws Exception {
        ViewGroup container = new FrameLayout(application);
        View view = (View) adapter.instantiateItem(container, 0);
        TextView text = (TextView) view.findViewById(R.id.address);
        assertThat(text.getText()).isEqualTo(SimpleFeature.fromFeature(feature).address());
    }

    @Test
    public void getCount_shouldReturnListSize() throws Exception {
        assertThat(adapter.getCount()).isEqualTo(1);
    }

    @Test
    public void isViewFromObject_shouldReturnTrueIfEqual() throws Exception {
        View view = new View(application);
        assertThat(adapter.isViewFromObject(view, view)).isTrue();
    }

    @Test
    public void isViewFromObject_shouldReturnFalseIfNotEqual() throws Exception {
        View view = new View(application);
        assertThat(adapter.isViewFromObject(view, new Object())).isFalse();
    }

    @Test
    public void destroyItem_shouldRemoveViewFromContainer() throws Exception {
        ViewGroup container = new FrameLayout(application);
        View view = new View(application);
        container.addView(view);
        adapter.destroyItem(container, 0, view);
        assertThat(container.getChildCount()).isEqualTo(0);
    }

    @Test
    public void onClick_shouldPostRoutePreviewEvent() throws Exception {
        View view = (View) adapter.instantiateItem(new FrameLayout(application), 0);
        ImageButton start = (ImageButton) view.findViewById(R.id.preview);
        RoutePreviewSubscriber subscriber = new RoutePreviewSubscriber();
        adapter.getBus().register(subscriber);
        start.performClick();
        assertThat(subscriber.event.getDestination().properties.label)
                .isEqualTo(getTestFeature().properties.label);
    }

    public static class RoutePreviewSubscriber {
        private RoutePreviewEvent event;

        @Subscribe
        public void onRoutePreviewEvent(RoutePreviewEvent event) {
            this.event = event;
        }
    }
}
