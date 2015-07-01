package com.mapzen.erasermap.shadows;

import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowSurfaceView;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

@Implements(GLSurfaceView.class)
public class ShadowGLSurfaceView extends ShadowSurfaceView {
    public void __constructor__(Context context, AttributeSet attributeSet) {
    }
}
