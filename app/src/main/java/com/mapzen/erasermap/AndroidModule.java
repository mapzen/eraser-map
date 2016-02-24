package com.mapzen.erasermap;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.erasermap.model.AndroidAppSettings;
import com.mapzen.erasermap.model.ApiKeys;
import com.mapzen.erasermap.model.AppSettings;
import com.mapzen.erasermap.model.MapzenLocation;
import com.mapzen.erasermap.model.MapzenLocationImpl;
import com.mapzen.erasermap.model.RouteManager;
import com.mapzen.erasermap.model.TileHttpHandler;
import com.mapzen.erasermap.model.ValhallaRouteManager;
import com.mapzen.erasermap.model.ValhallaRouterFactory;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.erasermap.presenter.ViewStateManager;

import com.squareup.otto.Bus;

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

    @Provides @Singleton MapzenLocation provideMapzenLocation(LostApiClient locationClient,
            AppSettings settings, Bus bus) {
        return new MapzenLocationImpl(locationClient, settings, bus, application);
    }

    @Provides @Singleton AppSettings provideAppSettings() {
        return new AndroidAppSettings(application);
    }

    @Provides @Singleton MainPresenter provideMainPresenter(MapzenLocation mapzenLocation, Bus bus,
            RouteManager routeManager, AppSettings settings, ViewStateManager vsm) {
        return new MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm);
    }

    @Provides @Singleton RouteManager provideRouteManager(AppSettings settings, ApiKeys keys) {
        final ValhallaRouteManager manager = new ValhallaRouteManager(settings,
                new ValhallaRouterFactory());
        manager.setApiKey(keys.getRoutingKey());
        return manager;
    }

    @Provides @Singleton TileHttpHandler provideTileHttpHandler(ApiKeys keys) {
        final TileHttpHandler handler = new TileHttpHandler(application);
        handler.setApiKey(keys.getTilesKey());
        return handler;
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }
}
