package com.example.rennt.arcticsunrise.data;

import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.rennt.arcticsunrise.ArcticSunriseModule;
import com.example.rennt.arcticsunrise.data.api.BaseApiPath;
import com.example.rennt.arcticsunrise.data.api.BaseEditionPath;
import com.example.rennt.arcticsunrise.data.api.CatalogService;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.ui.MainActivity;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.simpleframework.xml.util.Resolver;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

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
    private Uri getCatalogUri(@BaseEditionPath Uri basePath) {
        int catalogVersion = 1;

        if (edition == Edition.USA) {
            catalogVersion = 2;
        }
        String catalogPath = String.format(CATALOG_PATH, catalogVersion);

        return Uri.withAppendedPath(basePath, catalogPath);
    }


    @Provides @Singleton
    CatalogService providesCatalogService(DataModule.NetworkResolver resolver, Gson gson,
                                          @BaseEditionPath Uri basePath,
                                          ConnectivityManager cm){
        Uri catalogUri = getCatalogUri(basePath);
        return new CatalogService(resolver, gson, catalogUri, edition, cm);
    }
}
