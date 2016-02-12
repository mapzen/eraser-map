package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.Properties;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Implements(MapData.class)
public class ShadowMapData {
    @RealObject
    private MapData realMapData;

    private String name;
    private List<LngLat> points = new ArrayList<>();
    private List<LngLat> line = new ArrayList<>();

    private static final HashMap<String, MapData> mapDataCollection = new HashMap<>();

    public void __constructor__(String name) {
        this.name = name;
        mapDataCollection.put(name, realMapData);
    }

    @Implementation
    public MapData addPoint(Properties properties, LngLat point) {
        points.add(point);
        return realMapData;
    }

    @Implementation
    public MapData addLine(Properties properties, List<LngLat> line) {
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

    public static final MapData getDataByName(String name) {
        return mapDataCollection.get(name);
    }
}
