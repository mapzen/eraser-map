package com.mapzen.erasermap.dummy;

import com.mapzen.pelias.SimpleFeature;
import com.mapzen.pelias.gson.Feature;
import com.mapzen.pelias.gson.Geometry;
import com.mapzen.pelias.gson.Properties;

import com.google.common.io.Files;

import android.location.Location;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.getProperty;

public class TestHelper {
    public static final String TEST_TEXT = "Text";
    public static final String TEST_LOCALITY = "Locality";
    public static final String TEST_LOCAL_ADMIN = "Local Admin";
    public static final String TEST_ADMIN1_ABBR = "Admin1 Abbr";

    public static Feature getTestFeature() {
        return getTestFeature(0.0, 0.0);
    }

    public static Feature getTestFeature(double lat, double lon) {
        Feature feature = new Feature();
        Properties properties = new Properties();
        properties.setText(TEST_TEXT);
        properties.setLocality(TEST_LOCALITY);
        properties.setLocalAdmin(TEST_LOCAL_ADMIN);
        properties.setAdmin1Abbr(TEST_ADMIN1_ABBR);
        feature.setProperties(properties);
        Geometry geometry = new Geometry();
        List<Double> coordinates = new ArrayList<>();
        coordinates.add(lon);
        coordinates.add(lat);
        geometry.setCoordinates(coordinates);
        feature.setGeometry(geometry);
        return feature;
    }

    public static SimpleFeature getTestSimpleFeature() {
        return SimpleFeature.fromFeature(getTestFeature());
    }

    public static SimpleFeature getTestSimpleFeature(double lat, double lon) {
        return SimpleFeature.fromFeature(getTestFeature(lat, lon));
    }

    public static String getFixture(String name) throws IOException {
        String fileName = getProperty("user.dir");
        File file = new File(fileName + "/src/test/java/fixtures/" + name + ".route");
        String content;
        content = Files.toString(file, Charset.defaultCharset());
        return content;
    }

    public static Location getTestLocation(double lon, double lat) {
        final Location location = new Location("test");
        location.setLongitude(lon);
        location.setLatitude(lat);
        return location;
    }

    public static Location getTestLocation() {
        final Location location = new Location("test");
        return location;
    }
}
