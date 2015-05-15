package com.mapzen.erasermap.shadows;

import com.mapzen.erasermap.dummy.TestMap;

import org.oscim.android.AndroidAssets;
import org.oscim.android.MapView;
import org.oscim.android.canvas.AndroidGraphics;
import org.oscim.map.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowRelativeLayout;

import android.content.Context;
import android.util.AttributeSet;

@Implements(MapView.class)
public class ShadowMapView extends ShadowRelativeLayout {
    private Map map;

    public void __constructor__(Context context, AttributeSet attributeSet) {
        AndroidGraphics.init();
        AndroidAssets.init(context);
    }

    @Implementation
    public Map map() {
        if (map == null) {
            map = new TestMap();
        }

        return map;
    }
}
