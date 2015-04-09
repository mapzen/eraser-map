package com.mapzen.privatemaps;

import com.mapzen.privatemaps.shadows.ShadowGLMatrix;
import com.mapzen.privatemaps.shadows.ShadowMapView;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.internal.bytecode.ClassInfo;
import org.robolectric.internal.bytecode.InstrumentingClassLoaderConfig;
import org.robolectric.internal.bytecode.ShadowMap;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PrivateMapsTestRunner extends RobolectricGradleTestRunner {
    private static final List<String> CUSTOM_SHADOW_TARGETS =
            Collections.unmodifiableList(Arrays.asList(
                    "org.oscim.android.MapView",
                    "org.oscim.renderer.GLMatrix"
            ));

    public PrivateMapsTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected ShadowMap createShadowMap() {
        return super.createShadowMap()
                .newBuilder()
                .addShadowClass(ShadowMapView.class)
                .addShadowClass(ShadowGLMatrix.class)
                .build();
    }

    @Override
    public InstrumentingClassLoaderConfig createSetup() {
        return new PrivateMapsInstrumentingClassLoaderConfig();
    }

    public class PrivateMapsInstrumentingClassLoaderConfig extends InstrumentingClassLoaderConfig {
        @Override
        public boolean shouldInstrument(ClassInfo classInfo) {
            return CUSTOM_SHADOW_TARGETS.contains(classInfo.getName())
                    || super.shouldInstrument(classInfo);        }
    }
}
