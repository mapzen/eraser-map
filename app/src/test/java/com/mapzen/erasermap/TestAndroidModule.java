package com.mapzen.erasermap;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.erasermap.model.MapzenLocation;
import com.mapzen.erasermap.model.MapzenLocationImpl;

import org.mockito.Mockito;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class TestAndroidModule {
    private final TestEraserMapApplication application;

    public TestAndroidModule(TestEraserMapApplication application) {
        this.application = application;
    }

    @Provides @Singleton @ForApplication Context provideApplicationContext() {
        return application;
    }

    @Provides @Singleton LostApiClient provideLocationClient() {
        return new LostApiClient.Builder(application).build();
    }

    @Provides @Singleton CrashReportService provideCrashReportService() {
        return Mockito.mock(CrashReportService.class);
    }

    @Provides @Singleton MapzenLocation provideMapzenLocation() {
        return new MapzenLocationImpl(application);
    }
}
