package com.mapzen.erasermap;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.erasermap.model.AndroidAppSettings;
import com.mapzen.erasermap.model.ApiKeys;
import com.mapzen.erasermap.model.AppSettings;
import com.mapzen.erasermap.model.IntentQueryParser;
import com.mapzen.erasermap.model.MapzenLocation;
import com.mapzen.erasermap.model.MapzenLocationImpl;
import com.mapzen.erasermap.model.PermissionManager;
import com.mapzen.erasermap.model.RouteManager;
import com.mapzen.erasermap.model.TileHttpHandler;
import com.mapzen.erasermap.model.ValhallaRouteManager;
import com.mapzen.erasermap.model.ValhallaRouterFactory;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.erasermap.presenter.ViewStateManager;
import com.mapzen.erasermap.util.IntentFactory;
import com.mapzen.erasermap.view.Speaker;
import com.mapzen.erasermap.view.SpeakerboxSpeaker;
import com.mapzen.pelias.Pelias;

import com.squareup.otto.Bus;

import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import retrofit.RestAdapter;

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
            AppSettings settings, Bus bus, PermissionManager permissionManager) {
        return new MapzenLocationImpl(locationClient, settings, bus, application, permissionManager);
    }

    @Provides @Singleton AppSettings provideAppSettings() {
        return new AndroidAppSettings(application);
    }

    @Provides @Singleton MainPresenter provideMainPresenter(MapzenLocation mapzenLocation, Bus bus,
            RouteManager routeManager, AppSettings settings, ViewStateManager vsm,
            IntentQueryParser intentQueryParser) {
        return new MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm,
                intentQueryParser);
    }

    @Provides @Singleton RouteManager provideRouteManager(AppSettings settings, ApiKeys apiKeys) {
        final ValhallaRouteManager manager = new ValhallaRouteManager(settings,
                new ValhallaRouterFactory());
        manager.setApiKey(apiKeys.getRoutingKey());
        return manager;
    }

    @Provides @Singleton TileHttpHandler provideTileHttpHandler(ApiKeys apiKeys) {
        final TileHttpHandler handler = new TileHttpHandler(application);
        handler.setApiKey(apiKeys.getTilesKey());
        return handler;
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }

    @Provides @Singleton Pelias providePelias() {
        final String endpoint = BuildConfig.SEARCH_BASE_URL != null ?
                BuildConfig.SEARCH_BASE_URL : Pelias.DEFAULT_SERVICE_ENDPOINT;
        final RestAdapter.LogLevel logLevel = BuildConfig.DEBUG ?
                RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.NONE;
        return Pelias.getPeliasWithEndpoint(endpoint, logLevel);
    }

    @Provides @Singleton Speaker provideSpeaker() {
        return new SpeakerboxSpeaker(application);
    }

    @Provides @Singleton IntentFactory provideIntentFactory() {
        return new IntentFactory();
    }

    @Provides @Singleton PermissionManager providePermissionManager() {
        return new PermissionManager();
    }

    @Provides @Singleton ApiKeys provideApiKeys() {
        return new ApiKeys(application);
    }

}
