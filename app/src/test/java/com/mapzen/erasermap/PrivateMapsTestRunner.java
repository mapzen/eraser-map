package com.mapzen.erasermap;

import com.mapzen.erasermap.shadows.ShadowGLSurfaceView;
import com.mapzen.erasermap.shadows.ShadowMapController;
import com.mapzen.erasermap.shadows.ShadowMapData;
import com.mapzen.erasermap.shadows.ShadowMapView;
import com.mapzen.erasermap.shadows.ShadowPorterDuffColorFilter;
import com.mapzen.erasermap.shadows.ShadowTangram;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.ShadowMap;

public class PrivateMapsTestRunner extends RobolectricGradleTestRunner {

    public PrivateMapsTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected ShadowMap createShadowMap() {
        return super.createShadowMap()
                .newBuilder()
                .addShadowClass(ShadowMapView.class)
                .addShadowClass(ShadowMapController.class)
                .addShadowClass(ShadowGLSurfaceView.class)
                .addShadowClass(ShadowPorterDuffColorFilter.class)
                .addShadowClass(ShadowMapData.class)
                .addShadowClass(ShadowTangram.class)
                .build();
    }

    @Override
    public InstrumentationConfiguration createClassLoaderConfig(Config config) {
        return InstrumentationConfiguration.newBuilder()
                .addInstrumentedPackage("com.mapzen.tangram")
                .build();
    }

}
