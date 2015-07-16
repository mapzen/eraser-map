package com.mapzen.erasermap;

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
}
