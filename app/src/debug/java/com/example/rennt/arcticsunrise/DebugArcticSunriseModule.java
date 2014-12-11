package com.example.rennt.arcticsunrise;

import com.example.rennt.arcticsunrise.ui.DebugUiModule;

import dagger.Module;

/**
 * Created by rennt on 12/10/14.
 */
@Module(
        addsTo = ArcticSunriseModule.class,
        includes = {
                DebugUiModule.class,
//                DebugDataModule.class
        },

        overrides = true
)
public class DebugArcticSunriseModule {}
