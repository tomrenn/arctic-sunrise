package com.example.rennt.arcticsunrise;

import android.app.Application;
import android.content.Context;

import dagger.ObjectGraph;
import dagger.internal.Modules;
import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by rennt on 11/16/14.
 */
public class ArcticSunriseApp extends Application{
    private ObjectGraph objectGraph;


    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }

        buildObjectGraphAndInject();

    }

    @DebugLog
    public void buildObjectGraphAndInject() {
        objectGraph = ObjectGraph.create(Modules.list(this));
//        objectGraph.inject(this);
        // we would inject this if we were injecting anything into App.java
    }

    public void inject(Object o) {
        objectGraph.inject(o);
    }

    public static ArcticSunriseApp get(Context context) {
        return (ArcticSunriseApp) context.getApplicationContext();
    }
}
