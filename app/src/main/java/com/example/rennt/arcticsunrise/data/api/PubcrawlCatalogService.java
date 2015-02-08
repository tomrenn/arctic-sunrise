package com.example.rennt.arcticsunrise.data.api;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.example.rennt.arcticsunrise.data.DataModule;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.google.gson.Gson;

import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import dagger.Provides;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


/**
 * Service for obtaining catalogs for a specific Edition.
 */
public class PubcrawlCatalogService implements CatalogService{
    private final DataModule.NetworkResolver resolver;
    private final Gson gson;
    private final Uri catalogAddress;
    private final Edition edition;
    private final ConnectivityManager connectivityManager;
    public static final String CATALOG_CACHE_FLAG = "catalogCacheFlag";

    public PubcrawlCatalogService(DataModule.NetworkResolver resolver, Gson gson,
                                  Uri catalogAddress, Edition edition,
                                  ConnectivityManager cm){
        this.resolver = resolver;
        this.gson = gson;
        this.catalogAddress = catalogAddress;
        this.edition = edition;
        this.connectivityManager = cm;
    }

    public Observable<Catalog> getCatalogObservable(){
        return getCatalogObservable(true);
    }

    /**
     * todo: better define/document code paths
     * Get cached catalog, cached issues through catalog id.
     * get new catalog - save new catalog with similar issues and remove tail issues and old catalog.
     *
     */
    public Observable<Catalog> getCatalogObservable(final boolean useCache) {
        return Observable.create(new Observable.OnSubscribe<Catalog>(){
            @Override
            @DebugLog
            public void call(final Subscriber<? super Catalog> subscriber) {
                try {
                    Catalog cachedCatalog = null;

                    if (useCache) {
                        List<Catalog> cachedCatalogs = Catalog.findByKey(Catalog.class, edition.ordinal());
                        if (cachedCatalogs != null && cachedCatalogs.size() > 0) {
                            Timber.d("(Cache) Obtained Catalog");
                            cachedCatalog = cachedCatalogs.get(0);
                            // lookup cached issues
                            List<Issue> issueSet = Issue.findByKey(Issue.class, cachedCatalog.getId());
                            Field _issues = Catalog.class.getDeclaredField("issues");
                            _issues.setAccessible(true);
                            _issues.set(cachedCatalog, issueSet);

                            subscriber.onNext(cachedCatalog);
                        }
                    }

                    // check network access
                    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                    if (networkInfo == null || !networkInfo.isConnected()){
                        Timber.d("No Internet connection found, stopping catalog lookup");
                        subscriber.onCompleted();
                        return;
                    }

                    Timber.d("Catalog request (network): " + catalogAddress.toString());

                    Reader responseStream = resolver.fetchUri(catalogAddress);
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
