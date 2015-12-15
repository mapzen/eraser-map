package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.DataSource;
import com.mapzen.tangram.DebugFlags;
import com.mapzen.tangram.Tangram;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowSurfaceView;

import java.util.ArrayList;
import java.util.HashSet;

@Implements(Tangram.class)
public class ShadowTangram extends ShadowSurfaceView {
    public static ArrayList<DataSource> dataSources = new ArrayList<>();
    public static HashSet<DebugFlags> debugFlags = new HashSet<>();

    @Implementation
    public static void addDataSource(DataSource _source) {
        dataSources.add(_source);
    }

    @Implementation
    public static void clearDataSource(DataSource _source, boolean _data, boolean _tiles) {
        dataSources.remove(_source);
    }

    @Implementation
    public static void setDebugFlag(DebugFlags _flag, boolean _on) {
        if (_on) {
            debugFlags.add(_flag);
        } else {
            debugFlags.remove(_flag);
        }
    }
}
