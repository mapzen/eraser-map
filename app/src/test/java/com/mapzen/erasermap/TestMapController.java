package com.mapzen.erasermap;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapData;
import com.mapzen.tangram.TestMapData;

import android.opengl.GLSurfaceView;

import java.util.HashSet;

public class TestMapController extends MapController {

    float zoom;
    LngLat position;

    public HashSet<MapData> dataSources = new HashSet<>();

    public TestMapController(GLSurfaceView view, String sceneFilePath) {
        super(view, sceneFilePath);
    }

    public MapData addDataLayer(String name) {
        TestMapData mapData = new TestMapData(name);
        dataSources.add(mapData);
        return mapData;
    }

    @Override
    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    @Override
    public float getZoom() {
        return this.zoom;
    }

    @Override
    public void setPosition(LngLat lngLat) {
        this.position = lngLat;
    }

    @Override
    public void setPositionEased(LngLat position, int duration) {
        this.position = position;
    }

    @Override
    public LngLat getPosition() {
        return this.position;
    }
}
