package com.mapzen.erasermap;

import android.content.Context;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.erasermap.model.AndroidAppSettings;
import com.mapzen.erasermap.model.AppSettings;
import com.mapzen.erasermap.model.MapzenLocation;
import com.mapzen.erasermap.model.MapzenLocationImpl;
import com.mapzen.erasermap.model.RouteManager;
import com.mapzen.erasermap.model.TileHttpHandler;
import com.mapzen.erasermap.model.ValhallaRouteManager;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.erasermap.presenter.ViewStateManager;
import com.mapzen.leyndo.ManifestModel;
import com.squareup.otto.Bus;

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

    @Provides @Singleton ManifestModel provideManifestModel() {
        return new ManifestModel();
    }

    @Provides @Singleton LostApiClient provideLocationClient() {
        return new LostApiClient.Builder(application).build();
    }

    @Provides @Singleton CrashReportService provideCrashReportService() {
        return new CrashReportService();
    }

    @Provides @Singleton MapzenLocation provideMapzenLocation(LostApiClient locationClient,
            AppSettings settings, Bus bus) {
        return new MapzenLocationImpl(locationClient, settings, bus);
    }

    @Provides @Singleton AppSettings provideAppSettings() {
        return new AndroidAppSettings(application);
    }

    @Provides @Singleton MainPresenter provideMainPresenter(MapzenLocation mapzenLocation, Bus bus,
            RouteManager routeManager, AppSettings settings, ViewStateManager vsm) {
        return new MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm);
    }

    @Provides @Singleton RouteManager provideRouteManager(AppSettings settings) {
        return new ValhallaRouteManager(settings);
    }

    @Provides @Singleton TileHttpHandler provideTileHttpHandler() {
        return new TileHttpHandler(application);
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }
}
