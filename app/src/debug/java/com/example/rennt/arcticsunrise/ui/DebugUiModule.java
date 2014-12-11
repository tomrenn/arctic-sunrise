package com.example.rennt.arcticsunrise.ui;

import com.example.rennt.arcticsunrise.AppContainer;
import com.example.rennt.arcticsunrise.ui.debug.DebugAppContainer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rennt on 12/9/14.
 */
@Module(
        injects = {
            DebugAppContainer.class
        },
        library = true,
        complete = false,
        overrides = true
)
public class DebugUiModule {

    /**
     * Returns the DebugAppContainer as an "AppContainer".
     * This module overrides providers in the main UiModule.class
     */
    @Provides @Singleton AppContainer provideAppContainer(DebugAppContainer debugAppContainer){
        return debugAppContainer;
    }
}
