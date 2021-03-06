package com.example.rennt.arcticsunrise;

import android.app.Application;

import com.example.rennt.arcticsunrise.data.DataModule;
import com.example.rennt.arcticsunrise.ui.UiModule;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rennt on 11/16/14.
 */
@Module(
        includes = {
            UiModule.class,
            DataModule.class
        }
)
public final class ArcticSunriseModule {
    private final ArcticSunriseApp app;

    public ArcticSunriseModule(ArcticSunriseApp app) {
        this.app = app;
    }

    @Provides @Singleton Application provideApplication() {
        return app;
    }

}