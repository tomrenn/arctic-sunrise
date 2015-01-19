package com.example.rennt.arcticsunrise;

import com.example.rennt.arcticsunrise.data.DebugDataModule;

/**
 * Created by rennt on 11/16/14.
 */
public class Modules {
    static Object[] list(ArcticSunriseApp app) {
        return new Object[] {
                new ArcticSunriseModule(app),
                // overriding debug module
                new DebugDataModule(),
                new DebugArcticSunriseModule(),
        };
    }

    private Modules() {
        // No instances.
    }
}
