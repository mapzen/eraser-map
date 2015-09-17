package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapData;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.ArrayList;
import java.util.List;

@Implements(MapData.class)
public class ShadowMapData {
    @RealObject private MapData realMapData;

    private String name;
    private List<LngLat> points;
    private List<LngLat> line;

    public void __constructor__(String name) {
        this.name = name;
    }

    @Implementation
    public MapData addPoint(LngLat point) {
        if (points == null) {
            points = new ArrayList<>();
        }

        points.add(point);
        return realMapData;
    }

    @Implementation
    public MapData addLine(List<LngLat> line) {
        this.line = line;
        return realMapData;
    }

    public String getName() {
        return name;
    }

    public List<LngLat> getPoints() {
        return points;
    }

    public List<LngLat> getLine() {
        return line;
    }
}
