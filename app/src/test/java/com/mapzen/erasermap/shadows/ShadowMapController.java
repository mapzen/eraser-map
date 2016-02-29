package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.LngLat;
import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapView;
import com.mapzen.tangram.TouchInput;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

@Implements(MapController.class)
public class ShadowMapController {
    private double lng;
    private double lat;
    private float zoom;
    private float rotation;
    private ArrayList<Runnable> eventQueue = new ArrayList<>();

    public void __constructor__(Activity mainApp, MapView view) {
    }

    public void __constructor__(Activity mainApp, MapView view, String sceneFilePath) {
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

    @Implementation
    public void queueEvent(Runnable r) {
        eventQueue.add(r);
    }

    public List<Runnable> getEventQueue() {
        return eventQueue;
    }

    @Implementation
    public void setLongPressResponder(final TouchInput.LongPressResponder responder) {
        // Do nothing
    }

    @Implementation
    public void setTapResponder(final TouchInput.TapResponder responder) {
        // Do nothing
    }

    @Implementation
    public void setDoubleTapResponder(final TouchInput.DoubleTapResponder responder) {
        // Do nothing
    }

    @Implementation
    public void setRotateResponder(final TouchInput.RotateResponder responder) {
        // Do nothing
    }

    @Implementation
    public void setPanResponder(final TouchInput.PanResponder responder) {
        // Do nothing
    }

    @Implementation
    public void requestRender() {
        // Do nothing
    }
}
