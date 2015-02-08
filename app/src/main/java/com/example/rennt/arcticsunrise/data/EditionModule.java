package com.example.rennt.arcticsunrise.data;

import android.net.ConnectivityManager;
import android.net.Uri;

import com.example.rennt.arcticsunrise.ArcticSunriseModule;
import com.example.rennt.arcticsunrise.data.api.BaseApiPath;
import com.example.rennt.arcticsunrise.data.api.BaseEditionPath;
import com.example.rennt.arcticsunrise.data.api.PubcrawlCatalogService;
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
    private static String CATALOG_PATH = "android.phone.wifi.%d.catalog.json";
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

    /**
     * Return the catalog address. '.../usa/catalog.json'
     */
    private Uri getCatalogUri(Uri basePath) {
        int catalogVersion = 1;

        if (edition == Edition.USA) {
            catalogVersion = 2;
        }
        String catalogPath = String.format(CATALOG_PATH, catalogVersion);

        return Uri.withAppendedPath(basePath, catalogPath);
    }


    @Provides @Singleton
    PubcrawlCatalogService providesCatalogService(DataModule.NetworkResolver resolver, Gson gson,
                                          @BaseEditionPath Uri basePath,
                                          ConnectivityManager cm){
        Uri catalogUri = getCatalogUri(basePath);
        return new PubcrawlCatalogService(resolver, gson, catalogUri, edition, cm);
    }
}
