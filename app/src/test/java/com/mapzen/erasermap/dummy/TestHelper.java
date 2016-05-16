package com.mapzen.erasermap.dummy;

import com.mapzen.pelias.SimpleFeature;
import com.mapzen.pelias.gson.Feature;
import com.mapzen.pelias.gson.Geometry;
import com.mapzen.pelias.gson.Properties;
import com.mapzen.valhalla.Instruction;

import com.google.common.io.Files;

import org.json.JSONException;
import org.json.JSONObject;
import org.mockito.Mockito;

import android.location.Location;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.getProperty;

public class TestHelper {
    public static final String TEST_NAME = "Name";
    public static final String TEST_LOCALITY = "Locality";
    public static final String TEST_LOCAL_ADMIN = "Local Admin";
    public static final String TEST_ADMIN1_ABBR = "Admin1 Abbr";
    public static final String TEST_LABEL = "Name, Local Admin, Admin1 Abbr";

    public static Feature getTestFeature() {
        return getTestFeature(0.0, 0.0);
    }

    public static Feature getTestFeature(double lat, double lon) {
        Feature feature = new Feature();
        Properties properties = new Properties();
        properties.name = TEST_NAME;
        properties.locality = TEST_LOCALITY;
        properties.localadmin = TEST_LOCAL_ADMIN;
        properties.region_a = TEST_ADMIN1_ABBR;
        properties.label = TEST_LABEL;
        feature.properties = properties;
        Geometry geometry = new Geometry();
        List<Double> coordinates = new ArrayList<>();
        coordinates.add(lon);
        coordinates.add(lat);
        geometry.coordinates = coordinates;
        feature.geometry = geometry;
        return feature;
    }

    public static SimpleFeature getTestSimpleFeature() {
        return SimpleFeature.fromFeature(getTestFeature());
    }

    public static SimpleFeature getTestSimpleFeature(double lat, double lon) {
        return SimpleFeature.fromFeature(getTestFeature(lat, lon));
    }

    public static String getFixture(String name) throws IOException {
        String base = getProperty("user.dir");
        String filename = base + "/src/test/java/com/mapzen/erasermap/fixtures/" + name + ".route";
        filename = filename.replace(".idea/modules/", "");
        File file = new File(filename);
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

    public static Instruction getTestInstruction() throws JSONException {
        JSONObject jsonObject = Mockito.mock(JSONObject.class);
        Mockito.when(jsonObject.length()).thenReturn(6);
        Mockito.when(jsonObject.getString("type")).thenReturn("0");
        return new Instruction(jsonObject);
    }

    public static Location getMockLocation(double lon, double lat, float bearing,
            boolean hasBearing) {
        Location location = Mockito.mock(Location.class);
        Mockito.when(location.getLongitude()).thenReturn(lon);
        Mockito.when(location.getLatitude()).thenReturn(lat);
        Mockito.when(location.getBearing()).thenReturn(bearing);
        Mockito.when(location.hasBearing()).thenReturn(hasBearing);
        return location;
    }

}
