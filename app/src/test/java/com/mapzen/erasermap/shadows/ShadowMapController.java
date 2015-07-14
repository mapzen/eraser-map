package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapView;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.app.Activity;

@Implements(MapController.class)
public class ShadowMapController {
    private double lon;
    private double lat;
    private float zoom;

    public void __constructor__(Activity mainApp, MapView view) {
    }

    @Implementation
    public void setMapPosition(double lon, double lat) {
        this.lon = lon;
        this.lat = lat;
    }

    @Implementation
    public double[] getMapPosition() {
        return new double[] {lon, lat};
    }

    @Implementation
    public void setMapZoom(float zoom) {
        this.zoom = zoom;
    }

    @Implementation
    public float getMapZoom() {
        return zoom;
    }
}
