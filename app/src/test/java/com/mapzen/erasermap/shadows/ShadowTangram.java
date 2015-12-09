package com.mapzen.erasermap.shadows;

import com.mapzen.tangram.DataSource;
import com.mapzen.tangram.Tangram;

import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowSurfaceView;

import java.util.ArrayList;

@Implements(Tangram.class)
public class ShadowTangram extends ShadowSurfaceView {
    public static ArrayList<DataSource> dataSources = new ArrayList<>();

    @Implementation
    public static void addDataSource(DataSource source) {
        dataSources.add(source);
    }

    @Implementation
    public static void clearDataSource(DataSource source, boolean data, boolean tiles) {
        dataSources.remove(source);
    }
}
