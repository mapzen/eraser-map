package com.mapzen.erasermap;

import android.content.Context;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.erasermap.model.AppSettings;
import com.mapzen.erasermap.model.MapzenLocation;
import com.mapzen.erasermap.model.MapzenLocationImpl;
import com.mapzen.erasermap.model.RouteManager;
import com.mapzen.erasermap.model.TestAppSettings;
import com.mapzen.erasermap.model.TestRouteManager;
import com.mapzen.erasermap.model.TileHttpHandler;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.erasermap.presenter.ViewStateManager;
import com.mapzen.leyndo.ManifestModel;
import com.squareup.otto.Bus;

import org.mockito.Mockito;

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

    @Provides @Singleton MapzenLocation provideMapzenLocation(LostApiClient locationClient,
            AppSettings settings, Bus bus) {
        return new MapzenLocationImpl(locationClient, settings, bus);
    }

    @Provides @Singleton AppSettings provideAppSettings() {
        return new TestAppSettings();
    }

    @Provides @Singleton MainPresenter provideMainPresenter(MapzenLocation mapzenLocation,
            RouteManager routeManager, AppSettings settings, ViewStateManager vsm) {
        return new MainPresenterImpl(mapzenLocation, routeManager, settings, vsm);
    }

    @Provides @Singleton RouteManager provideRouteManager() {
        return new TestRouteManager();
    }

    @Provides @Singleton TileHttpHandler provideTileHttpHandler() {
        return new TileHttpHandler(application);
    }

    @Provides @Singleton ManifestModel provideManifestModel() {
        return new ManifestModel();
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }
}
