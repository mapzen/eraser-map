package com.mapzen.erasermap;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.erasermap.model.AppSettings;
import com.mapzen.erasermap.model.MapzenLocation;
import com.mapzen.erasermap.model.MapzenLocationImpl;
import com.mapzen.erasermap.model.RouterFactory;
import com.mapzen.erasermap.model.TestAppSettings;
import com.mapzen.erasermap.model.TestRouterFactory;
import com.mapzen.erasermap.model.TileHttpHandler;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.erasermap.presenter.RoutePresenter;
import com.mapzen.erasermap.presenter.RoutePresenterImpl;
import com.mapzen.helpers.RouteEngine;

import com.squareup.otto.Bus;

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

    @Provides @Singleton MapzenLocation provideMapzenLocation(LostApiClient locationClient,
            AppSettings settings, Bus bus) {
        return new MapzenLocationImpl(locationClient, settings, bus);
    }

    @Provides @Singleton AppSettings provideAppSettings() {
        return new TestAppSettings();
    }

    @Provides @Singleton MainPresenter provideMainPresenter(MapzenLocation mapzenLocation,
            RouterFactory routerFactory, AppSettings settings) {
        return new MainPresenterImpl(mapzenLocation, routerFactory, settings);
    }

    @Provides @Singleton RoutePresenter provideRoutePresenter(RouteEngine routeEngine,
            MapzenLocation mapzenLocation) {
        return new RoutePresenterImpl(routeEngine, mapzenLocation);
    }

    @Provides @Singleton RouterFactory provideRouterFactory() {
        return new TestRouterFactory();
    }

    @Provides @Singleton TileHttpHandler provideTileHttpHandler() {
        return new TileHttpHandler(application);
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }
}
