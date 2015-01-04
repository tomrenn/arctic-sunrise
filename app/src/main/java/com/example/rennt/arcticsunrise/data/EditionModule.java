package com.example.rennt.arcticsunrise.data;

import com.example.rennt.arcticsunrise.ArcticSunriseModule;
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
    private static String NEW_FORMAT = "http://%s/gc/packager/wsj/%s";
    private static String NEW_DOMAIN = "gelcap.dowjones.com";
    private static String OLD_FORMAT = "http://%s/gc/packager/%s";
    private static String OLD_DOMAIN = "mitp.wsj.com";
    private static String CATALOG_PATH = "/android.phone.wifi.%d.catalog.json";
    private Edition edition;


    public EditionModule(Edition edition){
        this.edition = edition;
    }

    /**
     * Determine the root url location based on edition.
     */
    @Provides @BaseEditionPath String getBasePath(Edition edition){
        String base;
        String domain;

        if (edition == Edition.USA ||
                edition == Edition.EUROPE ||
                edition == Edition.ASIA){
            base = NEW_FORMAT;
            domain = NEW_DOMAIN;
        }
        else {
            base = OLD_FORMAT;
            domain = OLD_DOMAIN;
        }
        return String.format(base, domain, edition.getPath());
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

        return basePath + catalogPath;
    }


    /**
     * Todo: make a catalogManager. getObservable(bool useCache) ?
     *
     * get cached catalog, cached issues through catalog id.
     *
     * get new catalog - save new catalog with similar issues and remove tail issues and old catalog.
     *
     */
    @Provides Observable<Catalog> provideCatalogObservable(final OkHttpClient httpClient, final Gson gson) {
        return Observable.create(new Observable.OnSubscribe<Catalog>(){
            @Override
            @DebugLog
            public void call(final Subscriber<? super Catalog> subscriber) {

                String address = getCatalogAddress(getBasePath(edition));
                try {
                    Catalog catalog;
                    List<Catalog> cachedCatalogs = Catalog.find(Catalog.class, "_edition = ?", edition.toString());
                    if (cachedCatalogs != null && cachedCatalogs.size() > 0){
                        Timber.d("(Cache) Obtained Catalog");
                        catalog = cachedCatalogs.get(0);
                        // lookup cached issues
                        List<Issue> issueSet = Issue.findByKey(Issue.class, catalog.getId());
                        Field _issues = Catalog.class.getDeclaredField("issues");
                        _issues.setAccessible(true);
                        _issues.set(catalog, issueSet);

                        subscriber.onNext(catalog);
                        subscriber.onCompleted();
                    }
                    Timber.d("Catalog request (network): " + address);

                    Reader responseStream = DataModule.fetchUri(httpClient, address);
                    catalog = gson.fromJson(responseStream, Catalog.class);

                    Field _editionField = Catalog.class.getDeclaredField("_edition");
                    _editionField.setAccessible(true);
                    _editionField.set(catalog, edition);

                    subscriber.onNext(catalog);

                    // save to database for future use
                    catalog.save();
                    for (Issue issue: catalog.getIssues()){
                        issue.saveWithKey(catalog.getId());
                    }

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
