package com.mapzen.erasermap;

import com.mapzen.erasermap.controller.MainActivity;
import com.mapzen.erasermap.model.ApiKeys;
import com.mapzen.erasermap.receiver.MockLocationReceiver;
import com.mapzen.erasermap.view.DistanceView;
import com.mapzen.erasermap.view.InitActivity;
import com.mapzen.erasermap.view.RouteModeView;
import com.mapzen.erasermap.view.RoutePreviewView;
import com.mapzen.erasermap.view.SearchResultsAdapter;
import com.mapzen.erasermap.view.SearchResultsView;
import com.mapzen.erasermap.view.SettingsActivity;
import com.mapzen.erasermap.view.ViewAboutPreference;
import com.mapzen.erasermap.view.VoiceNavigationController;

import android.app.Application;

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
        void inject(VoiceNavigationController controller);
        void inject(MockLocationReceiver receiver);
        void inject(ViewAboutPreference viewAboutPreference);
        void inject(SearchResultsView searchResultsView);
        void inject(RoutePreviewView routePreviewView);
    }

    private ApplicationComponent component;
    private boolean isVisible = true;

    @Override
    public void onCreate() {
        super.onCreate();
        ApiKeys.Companion.sharedInstance(this);
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
