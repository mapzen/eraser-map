package com.mapzen.erasermap;

import com.mapzen.erasermap.model.ManifestModel;
import com.mapzen.erasermap.presenter.MainPresenter;
import com.mapzen.erasermap.presenter.MainPresenterImpl;
import com.mapzen.helpers.RouteEngine;
import com.mapzen.pelias.SavedSearch;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class CommonModule {
    @Provides @Singleton SavedSearch provideSavedSearch() {
        return new SavedSearch();
    }

    @Provides @Singleton MainPresenter provideMainPresenter() {
        return new MainPresenterImpl();
    }

    @Provides @Singleton Bus provideBus() {
        return new Bus();
    }

    @Provides @Singleton ManifestModel provideApiKeys() {return new ManifestModel();}

        @Provides RouteEngine provideRouteEngine() {
        return new RouteEngine();
    }

}
