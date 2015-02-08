package com.example.rennt.arcticsunrise;

import android.content.Context;

import com.example.rennt.arcticsunrise.data.EditionModule;
import com.example.rennt.arcticsunrise.data.IssueModule;
import com.orm.SugarApp;

import dagger.ObjectGraph;
import hugo.weaving.DebugLog;
import timber.log.Timber;

/**
 * Created by rennt on 11/16/14.
 */
public class ArcticSunriseApp extends SugarApp {
    private ObjectGraph currentGraph;
    // basically a stack.
    private ObjectGraph baseGraph;
    private ObjectGraph editionGraph;
    private ObjectGraph issueGraph;
    // handle the object graphs here...

    @Override public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            // TODO Crashlytics.start(this);
            // TODO Timber.plant(new CrashlyticsTree());
        }

        buildObjectGraph();
//        objectGraph.plus(new DataModule(Edition.USA));
//        objectGraph.plus();
    }

    @DebugLog
    public void buildObjectGraph() {
        baseGraph = ObjectGraph.create(Modules.list(this));
        currentGraph = baseGraph;
        // we would inject this if we were injecting anything into App.java
//        objectGraph.inject(this);
    }

    public void inject(Object o) {
        currentGraph.inject(o);
    }

    public ObjectGraph plusEditionModule(EditionModule module){
        editionGraph = baseGraph.plus(module);
        currentGraph = editionGraph;
        return editionGraph;
    }

    public ObjectGraph plusIssueModule(IssueModule module){
        issueGraph = editionGraph.plus(module);
        currentGraph = issueGraph;
        return currentGraph;
    }

    public static ArcticSunriseApp get(Context context) {
        return (ArcticSunriseApp) context.getApplicationContext();
    }
}
