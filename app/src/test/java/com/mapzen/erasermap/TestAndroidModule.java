package com.mapzen.erasermap;

import com.mapzen.android.search.MapzenSearch;
import com.mapzen.erasermap.model.ApiKeys;
import com.mapzen.erasermap.model.AppSettings;
import com.mapzen.erasermap.model.IntentQueryParser;
import com.mapzen.erasermap.model.LocationConverter;
import com.mapzen.erasermap.model.LocationSettingsChecker;
import com.mapzen.erasermap.model.LostClientManager;
import com.mapzen.erasermap.model.LostFactory;
import com.mapzen.erasermap.model.MapzenLocation;
import com.mapzen.erasermap.model.MapzenLocationImpl;
import com.mapzen.erasermap.model.PermissionManager;
import com.mapzen.erasermap.model.RouteManager;
import com.mapzen.erasermap.model.TestAppSettings;
import com.mapzen.erasermap.model.TestLostSettingsChecker;
import com.mapzen.erasermap.model.TestRouteManager;
import com.mapzen.erasermap.model.TileHttpHandler;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.erasermap.presenter.ViewStateManager;
import com.mapzen.erasermap.util.IntentFactory;
import com.mapzen.erasermap.util.MockIntentFactory;
import com.mapzen.erasermap.view.Speaker;
import com.mapzen.erasermap.view.TestSpeakerbox;

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

    @Provides @Singleton LostClientManager provideLocationClientManager() {
        return new LostClientManager(application, new LostFactory());
    }

    @Provides @Singleton CrashReportService provideCrashReportService() {
        return Mockito.mock(CrashReportService.class);
    }

    @Provides @Singleton MapzenLocation provideMapzenLocation(
        LostClientManager locationClientManager, AppSettings settings, Bus bus,
        PermissionManager permissionsManager) {
        return new MapzenLocationImpl(locationClientManager, settings, bus, application,
                permissionsManager);
    }

    @Provides @Singleton AppSettings provideAppSettings() {
        return new TestAppSettings();
    }

    @Provides @Singleton MainPresenter provideMainPresenter(MapzenLocation mapzenLocation, Bus bus,
            RouteManager routeManager, AppSettings settings, ViewStateManager vsm,
            IntentQueryParser intentQueryParser, LocationConverter converter,
        LostClientManager lostClientManager, LocationSettingsChecker locationSettingsChecker) {
        return new MainPresenterImpl(mapzenLocation, bus, routeManager, settings, vsm,
                intentQueryParser, converter, lostClientManager, locationSettingsChecker);
    }

    @Provides @Singleton RouteManager provideRouteManager() {
        return new TestRouteManager();
    }

    @Provides @Singleton TileHttpHandler provideTileHttpHandler() {
        TileHttpHandler handler = new TileHttpHandler(application);
        handler.setApiKey(BuildConfig.API_KEY);
        return handler;
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }

    @Provides @Singleton MapzenSearch provideMapzenSearch() {
        return Mockito.mock(MapzenSearch.class);
    }

    @Provides @Singleton Speaker provideSpeakerbox() {
        return new TestSpeakerbox();
    }

    @Provides @Singleton IntentFactory provideIntentFactory() {
        return new MockIntentFactory();
    }

    @Provides @Singleton PermissionManager providePermissionManager() {
        return new PermissionManager();
    }

    @Provides @Singleton ApiKeys provideApiKeys() {
        return ApiKeys.Companion.sharedInstance(application);
    }

    @Provides @Singleton LocationConverter provideLocationConverter() {
        return new LocationConverter();
    }

    @Provides @Singleton LocationSettingsChecker provideLocationSettingsChecker() {
        return new TestLostSettingsChecker();
    }
}
