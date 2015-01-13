package com.example.rennt.arcticsunrise.data;

import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.rennt.arcticsunrise.ArcticSunriseModule;
import com.example.rennt.arcticsunrise.data.api.BaseApiPath;
import com.example.rennt.arcticsunrise.data.api.BaseEditionPath;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.ui.MainActivity;
import com.google.gson.Gson;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;

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
    private static String CATALOG_PATH = "/android.phone.wifi.%d.catalog.json";
    private Edition edition;


    public EditionModule(Edition edition){
        this.edition = edition;
    }

    /**
     * Determine the root url location based on edition.
     */
    @Provides @BaseEditionPath String getBasePath(Edition edition, @BaseApiPath String basePath){
        return basePath + "/" + edition.getPath();
    }

    /**
     * Return the catalog address.
     */
    private String getCatalogAddress(String basePath){
        int catalogVersion = 1;

        if (edition == Edition.USA) {
            catalogVersion = 2;
        }
        String catalogPath = String.format(CATALOG_PATH, catalogVersion);

        return basePath + "/" + catalogPath;
    }


    /**
     * Todo: make a catalogManager. getObservable(bool useCache) ?
     *
     * get cached catalog, cached issues through catalog id.
     *
     * get new catalog - save new catalog with similar issues and remove tail issues and old catalog.
     *
     */
    @Provides Observable<Catalog> provideCatalogObservable(final OkHttpClient httpClient, final Gson gson,
                                                           @BaseEditionPath final String basePath, final NetworkInfo networkInfo) {
        return Observable.create(new Observable.OnSubscribe<Catalog>(){
            @Override
            @DebugLog
            public void call(final Subscriber<? super Catalog> subscriber) {

                String address = getCatalogAddress(basePath);
                try {
                    Catalog cachedCatalog = null;
                    List<Catalog> cachedCatalogs = Catalog.findByKey(Catalog.class, edition.ordinal());
                    if (cachedCatalogs != null && cachedCatalogs.size() > 0){
                        Timber.d("(Cache) Obtained Catalog");
                        cachedCatalog = cachedCatalogs.get(0);
                        // lookup cached issues
                        List<Issue> issueSet = Issue.findByKey(Issue.class, cachedCatalog.getId());
                        Field _issues = Catalog.class.getDeclaredField("issues");
                        _issues.setAccessible(true);
                        _issues.set(cachedCatalog, issueSet);

                        subscriber.onNext(cachedCatalog);
                    }
                    // no internet connection
                    if (networkInfo == null || !networkInfo.isConnected()){
                        Timber.d("No Internet connection found, stopping catalog lookup");
                        subscriber.onCompleted();
                        return;
                    }
                    Timber.d("Catalog request (network): " + address);

                    Reader responseStream = DataModule.fetchUri(httpClient, address);
                    Catalog catalog = gson.fromJson(responseStream, Catalog.class);

                    int numNewIssues = 0;
                    if (cachedCatalog != null) {
                        // assign issue id to newly parsed issues if they already exist.
                        for (Issue issue : catalog.getIssues()){
                            Issue existingIssue = cachedCatalog.containsIssue(issue);
                            if (existingIssue != null){
                                issue.setId(existingIssue.getId());
                            } else {
                                numNewIssues++;
                            }
                        }
                        cachedCatalog.delete();
                    }
                    Timber.d("Catalog updated with " + numNewIssues + " new issues");

                    // save to database for future use
                    catalog.saveWithKey(edition.ordinal());
                    for (Issue issue: catalog.getIssues()){
                        issue.saveWithKey(catalog.getId());
                    }
                    subscriber.onNext(catalog);

                    subscriber.onCompleted();

                } catch (Exception e){
                    Timber.e(e.toString());
                    subscriber.onError(e);
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


}
