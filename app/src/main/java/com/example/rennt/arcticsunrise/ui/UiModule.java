package com.example.rennt.arcticsunrise.ui;

import com.example.rennt.arcticsunrise.AppContainer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rennt on 12/9/14.
 */
@Module(
        library = true,
        complete = false
)
public class UiModule {

    @Provides
    @Singleton
    AppContainer provideAppContainer(){
        return AppContainer.DEFAULT;
    }
}
