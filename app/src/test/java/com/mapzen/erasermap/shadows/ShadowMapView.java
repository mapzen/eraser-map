package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.MapView;

import org.robolectric.annotation.Implements;

import android.content.Context;
import android.util.AttributeSet;

@Implements(MapView.class)
public class ShadowMapView extends ShadowGLSurfaceView {
    public void __constructor__(Context context, AttributeSet attributeSet) {
    }
}
