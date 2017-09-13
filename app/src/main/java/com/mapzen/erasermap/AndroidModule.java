package com.mapzen.erasermap;

import com.mapzen.android.search.MapzenSearch;
import com.mapzen.android.search.MapzenSearchHttpHandler;
import com.mapzen.erasermap.model.AndroidAppSettings;
import com.mapzen.erasermap.model.ApiKeys;
import com.mapzen.erasermap.model.AppSettings;
import com.mapzen.erasermap.model.ConfidenceHandler;
import com.mapzen.erasermap.model.Http;
import com.mapzen.erasermap.model.IntentQueryParser;
import com.mapzen.erasermap.model.LocationConverter;
import com.mapzen.erasermap.model.LocationSettingsChecker;
import com.mapzen.erasermap.model.LostClientManager;
import com.mapzen.erasermap.model.LostFactory;
import com.mapzen.erasermap.model.LostSettingsChecker;
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
import com.mapzen.erasermap.util.FeatureDisplayHelper;
import com.mapzen.erasermap.util.IntentFactory;
import com.mapzen.erasermap.view.Speaker;
import com.mapzen.erasermap.view.SpeakerboxSpeaker;
import com.mapzen.pelias.Pelias;

import com.squareup.otto.Bus;

import android.content.Context;
import android.os.Build;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidModule {
    private static final int KITKAT = 19;

    private final EraserMapApplication application;

    public AndroidModule(EraserMapApplication application) {
        this.application = application;
    }

    @Provides @Singleton @ForApplication Context provideApplicationContext() {
        return application;
    }

    @Provides @Singleton LostClientManager provideLocationClientManager() {
        return new LostClientManager(application, new LostFactory());
    }

    @Provides @Singleton CrashReportService provideCrashReportService() {
        return new CrashReportService();
    }

    @Provides @Singleton MapzenLocation provideMapzenLocation(
        LostClientManager locationClientManager, AppSettings settings, Bus bus,
        PermissionManager permissionManager) {
        return new MapzenLocationImpl(locationClientManager, settings, bus, application,
            permissionManager);
    }

    @Provides @Singleton AppSettings provideAppSettings() {
        return new AndroidAppSettings(application);
    }

    @Provides @Singleton MainPresenter provideMainPresenter(MapzenLocation mapzenLocation, Bus bus,
            RouteManager routeManager, AppSettings settings, ViewStateManager vsm,
            IntentQueryParser intentQueryParser, LocationConverter converter,
            LostClientManager clientManager, LocationSettingsChecker locationSettingsChecker,
            PermissionManager permissionManager, ConfidenceHandler confidenceHandler) {
        return new MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm,
                intentQueryParser, converter, clientManager, locationSettingsChecker,
                permissionManager, confidenceHandler);
    }

    @Provides @Singleton RouteManager provideRouteManager(AppSettings settings) {
        return new ValhallaRouteManager(settings,
                new ValhallaRouterFactory(), application.getApplicationContext());
    }

    @Provides @Singleton TileHttpHandler provideTileHttpHandler() {
        final TileHttpHandler handler;
        if (Build.VERSION.SDK_INT >= KITKAT) {
            File httpCache = new File(application.getExternalCacheDir().getAbsolutePath() +
                "/tile_cache");
            int size = 30 * 1024 * 1024;
            handler = new TileHttpHandler(httpCache, size);
        } else {
            handler = new TileHttpHandler();
        }
        return handler;
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }

    @Provides @Singleton MapzenSearch provideMapzenSearch() {
        final String endpoint = BuildConfig.SEARCH_BASE_URL != null ?
                BuildConfig.SEARCH_BASE_URL : Pelias.DEFAULT_SEARCH_ENDPOINT;
        MapzenSearch search = new MapzenSearch(application);
        search.setHttpHandler(new MapzenSearchHttpHandler(application) {
            @Override public Map<String, String> queryParamsForRequest() {
                return null;
            }

            @Override public Map<String, String> headersForRequest() {
                Map<String, String> headers = new HashMap<>();
                headers.put(Http.HEADER_DNT, Http.VALUE_HEADER_DNT);
                return headers;
            }
        });
        Pelias pelias = search.getPelias();
        pelias.setEndpoint(endpoint);
        pelias.setDebug(BuildConfig.DEBUG);
        return search;
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

    @Provides @Singleton LocationConverter provideLocationConverter() {
        return new LocationConverter();
    }

    @Provides @Singleton LocationSettingsChecker provideLocationSettingsChecker() {
        return new LostSettingsChecker();
    }

    @Provides @Singleton ConfidenceHandler provideConfidenceHandler() {
        return new ConfidenceHandler();
    }

    @Provides @Singleton FeatureDisplayHelper provideDisplayHelper(
        ConfidenceHandler confidenceHandler) {
        return new FeatureDisplayHelper(application, confidenceHandler);
    }
}
