package com.mapzen.privatemaps;

import com.mapzen.pelias.gson.Feature;
import com.mapzen.pelias.gson.Geometry;
import com.mapzen.pelias.gson.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.RuntimeEnvironment.application;

@RunWith(PrivateMapsTestRunner.class)
@Config(constants = BuildConfig.class, emulateSdk = 21)
public class SearchResultsAdapterTest {
    private SearchResultsAdapter adapter;
    private Feature feature;

    @Before
    public void setUp() throws Exception {
        feature = getTestFeature();
        ArrayList<Feature> features = new ArrayList<>();
        features.add(feature);
        adapter = new SearchResultsAdapter(application, features);
    }

    @Test
    public void instantiateItem_shouldPopulateTitle() throws Exception {
        ViewGroup container = new FrameLayout(application);
        View view = (View) adapter.instantiateItem(container, 0);
        TextView text = (TextView) view.findViewById(R.id.title);
        assertThat(text.getText()).isEqualTo(SimpleFeature.fromFeature(feature).getTitle());
    }

    @Test
    public void instantiateItem_shouldPopulateAddress() throws Exception {
        ViewGroup container = new FrameLayout(application);
        View view = (View) adapter.instantiateItem(container, 0);
        TextView text = (TextView) view.findViewById(R.id.address);
        assertThat(text.getText()).isEqualTo(SimpleFeature.fromFeature(feature).getAddress());
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

    public static Feature getTestFeature() {
        return getTestFeature(0.0, 0.0);
    }

    public static Feature getTestFeature(double lat, double lon) {
        Feature feature = new Feature();
        Properties properties = new Properties();
        properties.setText("Text");
        properties.setLocality("Locality");
        properties.setLocalAdmin("Local Admin");
        properties.setAdmin1Abbr("Admin1 Abbr");
        feature.setProperties(properties);
        Geometry geometry = new Geometry();
        List<Double> coordinates = new ArrayList<Double>();
        coordinates.add(lon);
        coordinates.add(lat);
        geometry.setCoordinates(coordinates);
        feature.setGeometry(geometry);
        return feature;
    }
}
