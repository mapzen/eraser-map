package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.Tangram;

import org.robolectric.annotation.Implements;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;

@Implements(Tangram.class)
public class ShadowTangram extends ShadowGLSurfaceView {
    public void __constructor__(Context context, AttributeSet attributeSet) {
    }

    public void setup(Activity mainApp) {
    }
}
