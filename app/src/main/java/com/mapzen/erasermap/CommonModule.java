package com.mapzen.erasermap;

import com.mapzen.erasermap.presenter.RouteEngineListener;
import com.mapzen.erasermap.presenter.RoutePresenter;
import com.mapzen.erasermap.presenter.RoutePresenterImpl;
import com.mapzen.helpers.RouteEngine;
import com.mapzen.pelias.SavedSearch;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class CommonModule {
    @Provides @Singleton SavedSearch provideSavedSearch() {
        return new SavedSearch();
    }

    @Provides @Singleton RouteEngine provideRouteEngine() {
        return new RouteEngine();
    }

    @Provides @Singleton RouteEngineListener provideRouteEngineListener() {
        return new RouteEngineListener();
    }

    @Provides @Singleton RoutePresenter provideRoutePresenter(RouteEngine routeEngine,
            RouteEngineListener routeEngineListener) {
        return new RoutePresenterImpl(routeEngine, routeEngineListener);
    }
}
