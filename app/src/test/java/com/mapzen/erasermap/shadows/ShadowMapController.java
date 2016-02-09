package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapView;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.app.Activity;

@Implements(MapController.class)
public class ShadowMapController {
    private double lng;
    private double lat;
    private float zoom;
    private float rotation;

    public void __constructor__(Activity mainApp, MapView view) {
    }

    @Implementation
    public void setMapPosition(double lng, double lat) {
        setMapPosition(lng, lat, 0);
    }

    @Implementation
    public void setMapPosition(double lng, double lat, float duration) {
        this.lng = lng;
        this.lat = lat;
    }

    @Implementation
    public LngLat getMapPosition() {
        return new LngLat(lng, lat);
    }

    @Implementation
    public void setMapZoom(float zoom) {
        this.zoom = zoom;
    }

    @Implementation
    public float getMapZoom() {
        return zoom;
    }

    @Implementation
    public void setMapRotation(float radians) {
        this.rotation = radians;
    }

    @Implementation
    public float getMapRotation() {
        return rotation;
    }
}
