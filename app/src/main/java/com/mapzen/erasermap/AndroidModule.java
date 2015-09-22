package com.mapzen.erasermap;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.erasermap.model.AndroidAppSettings;
import com.mapzen.erasermap.model.AppSettings;
import com.mapzen.erasermap.model.MapzenLocation;
import com.mapzen.erasermap.model.MapzenLocationImpl;
import com.mapzen.erasermap.model.RouterFactory;
import com.mapzen.erasermap.model.ValhallaRouterFactory;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.erasermap.presenter.RoutePresenter;
import com.mapzen.erasermap.presenter.RoutePresenterImpl;
import com.mapzen.helpers.RouteEngine;

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
        return new MapzenLocationImpl(application);
    }

    @Provides @Singleton AppSettings provideAppSettings() {
        return new AndroidAppSettings(application);
    }

    @Provides @Singleton MainPresenter provideMainPresenter(MapzenLocation mapzenLocation,
            RouterFactory routerFactory, AppSettings settings) {
        return new MainPresenterImpl(mapzenLocation, routerFactory, settings);
    }

    @Provides @Singleton RoutePresenter provideRoutePresenter(RouteEngine routeEngine) {
        return new RoutePresenterImpl(routeEngine);
    }

    @Provides @Singleton RouterFactory provideRouterFactory() {
        return new ValhallaRouterFactory();
    }
}
