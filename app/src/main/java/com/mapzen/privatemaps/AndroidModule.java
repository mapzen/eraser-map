package com.mapzen.privatemaps;

import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.pelias.SavedSearch;

import com.squareup.okhttp.HttpResponseCache;

import android.content.Context;
import android.support.annotation.Nullable;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidModule {
    private final PrivateMapsApplication application;

    public AndroidModule(PrivateMapsApplication application) {
        this.application = application;
    }

    @Provides @Singleton @ForApplication Context provideApplicationContext() {
        return application;
    }

    @Provides @Singleton LostApiClient provideLocationClient() {
        return new LostApiClient.Builder(application).build();
    }

    @Provides @Singleton @Nullable HttpResponseCache provideTileCache() {
        return new TileCacheBuilder(application).build();
    }

    @Provides @Singleton SavedSearch provideSavedSearch() {
        return new SavedSearch();
    }

    @Provides @Singleton MainPresenter provideMainPresenter() {
        return new MainPresenterImpl();
    }

    @Provides @Singleton MarkerSymbolFactory provideMarkerSymbolFactory() {
        return new MarkerSymbolFactory(application);
    }
}
