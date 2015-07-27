package com.mapzen.erasermap;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.erasermap.model.MapzenLocation;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidModule {
    private final EraserMapApplication application;

    public AndroidModule(EraserMapApplication application) {
        this.application = application;
    }

    @Provides @Singleton @ForApplication Context provideApplicationContext() {
        return application;
    }

    @Provides @Singleton LostApiClient provideLocationClient() {
        return new LostApiClient.Builder(application).build();
    }

    @Provides @Singleton CrashReportService provideCrashReportService() {
        return new CrashReportService();
    }

    @Provides @Singleton MapzenLocation provideMapzenLocation() {
        return new MapzenLocation(application);
    }
}
