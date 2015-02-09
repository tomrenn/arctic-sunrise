package com.example.rennt.arcticsunrise.data;

import android.net.ConnectivityManager;
import android.net.Uri;

import com.example.rennt.arcticsunrise.ArcticSunriseModule;
import com.example.rennt.arcticsunrise.data.api.BaseApiPath;
import com.example.rennt.arcticsunrise.data.api.BaseEditionPath;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.ui.MainActivity;
import com.google.gson.Gson;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Provides an edition and a Catalog Observable for that edition.
 *
 * Created by rennt on 12/5/14.
 */
@Module(
    injects = MainActivity.class,
    addsTo = ArcticSunriseModule.class,
    complete = false, library = true
)
public class EditionModule {
    private Edition edition;


    public EditionModule(Edition edition){
        this.edition = edition;
    }

    /**
     * Get the base Edition path, e.g., '.../usa' or '.../europe'
     */
    @Provides @BaseEditionPath Uri getBasePath(@BaseApiPath Uri basePath){
        return Uri.withAppendedPath(basePath, edition.getPath());
    }

}
