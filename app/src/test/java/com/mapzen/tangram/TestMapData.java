package com.mapzen.tangram;

import com.mapzen.tangram.geometry.Geometry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TestMapData extends MapData {

    private String name;
    private List<LngLat> points = new ArrayList<>();
    private List<List<LngLat>> line = new ArrayList<>();

    TestMapData(String name, long pointer, MapController map) {
        super(name, pointer, map);
        this.name = name;
    }

    public TestMapData(String name) {
        super(name, -1, null);
        this.name = name;
    }

    @Override protected void addFeature(Geometry geometry) {

    }

    @Override public MapData addGeoJson(String data) {
        return this;
    }

    @Override public MapData addPoint(LngLat point, Map<String, String> properties) {
        points.add(point);
        return this;
    }

    @Override public MapData addPolygon(List<List<LngLat>> polygon,
            Map<String, String> properties) {
        return this;
    }

    @Override public MapData addPolyline(List<LngLat> polyline, Map<String, String> properties) {
        line.add(polyline);
        return this;
    }

    @Override
    public MapData clear() {
        points.clear();
        line.clear();
        return this;
    }

    public String getName() {
        return name;
    }

    public List<LngLat> getPoints() {
        return points;
    }

    public List<List<LngLat>> getLine() {
        return line;
    }
}
