package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.MapController;
import com.mapzen.tangram.MapView;

import org.robolectric.annotation.Implements;

import android.app.Activity;

@Implements(MapController.class)
public class ShadowMapController {
    public void __constructor__(Activity mainApp, MapView view) {
    }
}
