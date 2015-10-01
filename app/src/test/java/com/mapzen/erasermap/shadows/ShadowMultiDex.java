package com.mapzen.erasermap.shadows;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import android.content.Context;
import android.support.multidex.MultiDex;

@Implements(MultiDex.class)
public class ShadowMultiDex {
    @Implementation
    public static void install(Context context) {
        // Do nothing.
    }
}
