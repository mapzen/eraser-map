package com.mapzen.erasermap;

import android.app.Application;

import com.mapzen.erasermap.view.DistanceView;
import com.mapzen.erasermap.view.InitActivity;
import com.mapzen.erasermap.view.MainActivity;
import com.mapzen.erasermap.view.RouteModeView;
import com.mapzen.erasermap.view.SearchResultsAdapter;
import com.mapzen.erasermap.view.SettingsActivity;

import javax.inject.Singleton;

import dagger.Component;

public class EraserMapApplication extends Application {
    @Singleton
    @Component(modules = { AndroidModule.class, CommonModule.class })
    public interface ApplicationComponent {
        void inject(MainActivity mainActivity);
        void inject(InitActivity initActivity);
        void inject(SearchResultsAdapter searchResultsAdapter);
        void inject(RouteModeView routeModeView);
        void inject(SettingsActivity settingsFragment);
        void inject(DistanceView distanceView);
    }

    private ApplicationComponent component;
    private boolean isVisible = true;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerEraserMapApplication_ApplicationComponent.builder()
                .androidModule(new AndroidModule(this))
                .build();
    }

    public void onActivityResume() {
        isVisible = true;
    }

    public void onActivityPause() {
        isVisible = false;
    }

    public boolean getApplicationVisibility() {
        return isVisible;
    }

    public ApplicationComponent component() {
        return component;
    }
}
