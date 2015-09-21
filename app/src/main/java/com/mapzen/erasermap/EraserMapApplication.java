package com.mapzen.erasermap;

import com.mapzen.erasermap.model.MapzenLocationImpl;
import com.mapzen.erasermap.view.MainActivity;
import com.mapzen.erasermap.view.RouteModeView;
import com.mapzen.erasermap.view.SearchResultsAdapter;
import com.mapzen.erasermap.view.SettingsActivity;

import android.app.Application;

import javax.inject.Singleton;

import dagger.Component;

public class EraserMapApplication extends Application {
    @Singleton
    @Component(modules = { AndroidModule.class, CommonModule.class })
    public interface ApplicationComponent {
        void inject(MainActivity mainActivity);
        void inject(SearchResultsAdapter searchResultsAdapter);
        void inject(RouteModeView routeModeView);
        void inject(MapzenLocationImpl mapzenLocation);
        void inject(SettingsActivity.SettingsFragment settingsFragment);
    }

    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerEraserMapApplication_ApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
    }

    public ApplicationComponent component() {
        return component;
    }
}
