package com.mapzen.privatemaps;

import com.mapzen.android.lost.api.LostApiClient;

import com.squareup.okhttp.HttpResponseCache;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AndroidModule {
    public static final String CACHE_DIR = "tile-cache";
    public static final int CACHE_SIZE = 1024 * 1024 * 10; // 10 Megs

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

    @Provides @Singleton HttpResponseCache provideTileCache() {
        final File externalCacheDir = application.getExternalCacheDir();
        if (externalCacheDir != null) {
            try {
                return new HttpResponseCache(new File(externalCacheDir, CACHE_DIR), CACHE_SIZE);
            } catch (IOException e) {
                Log.e(PrivateMapsApplication.TAG, "Unable to create tile cache", e);
            }
        }

        return null;
    }
}
