package com.mapzen.privatemaps.shadows;

import org.mockito.Mockito;
import org.oscim.android.AndroidAssets;
import org.oscim.android.AndroidMap;
import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.map.Map;
import org.oscim.map.TestViewport;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadows.ShadowRelativeLayout;
import org.robolectric.util.ReflectionHelpers;

import android.content.Context;
import android.util.AttributeSet;

@Implements(MapView.class)
public class ShadowMapView extends ShadowRelativeLayout {
    @RealObject MapView realMapView;

    private static AndroidMap map;

    public void __constructor__(Context context, AttributeSet attributeSet) {
        AndroidGraphics.init();
        AndroidAssets.init(context);
        ReflectionHelpers.setField(realMapView, "mMap", getMockAndroidMap());
    }

    @Implementation
    public Map map() {
        return getMockAndroidMap();
    }

    public static Map getMockAndroidMap() {
        if (map == null) {
            map = Mockito.mock(AndroidMap.class);
            Mockito.when(map.viewport()).thenReturn(new TestViewport());
        }

        return map;
    }
}
