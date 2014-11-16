package com.example.rennt.arcticsunrise;

/**
 * Created by rennt on 11/16/14.
 */
public class Modules {
    static Object[] list(ArcticSunriseApp app) {
        return new Object[] {
                new ArcticSunriseModule(app),
                // debug module
        };
    }

    private Modules() {
        // No instances.
    }
}
