package com.mapzen.erasermap;

import android.content.Context;
import android.test.mock.MockContext;

import javax.inject.Singleton;

import dagger.Component;

public class TestEraserMapApplication extends EraserMapApplication {
    @Singleton
    @Component(modules = { TestAndroidModule.class, CommonModule.class })
    public interface TestApplicationComponent extends ApplicationComponent {
    }

    private TestApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerTestEraserMapApplication_TestApplicationComponent.builder()
                .testAndroidModule(new TestAndroidModule(this))
                .build();
    }

    @Override
    public ApplicationComponent component() {
        return component;
    }

    @Override public Context getApplicationContext() {
        return new MockContext();
    }
}
