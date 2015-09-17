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
    @RealObject
    private MapData realMapData;

    private String name;
    private List<LngLat> points = new ArrayList<>();
    private List<LngLat> line = new ArrayList<>();

    public void __constructor__(String name) {
        this.name = name;
    }

    @Implementation
    public MapData addPoint(LngLat point) {
        points.add(point);
        return realMapData;
    }

    @Implementation
    public MapData addLine(List<LngLat> line) {
        this.line = line;
        return realMapData;
    }

    @Implementation
    public MapData clear() {
        points.clear();
        line.clear();
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
