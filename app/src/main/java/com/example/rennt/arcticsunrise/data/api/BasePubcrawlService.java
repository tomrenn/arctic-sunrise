package com.example.rennt.arcticsunrise.data.api;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by tomrenn on 2/8/15.
 */
public class BasePubcrawlService implements PubcrawlService {
    private static String CATALOG_PATH = "android.phone.wifi.%d.catalog.json";

    private ConnectivityManager connectivityManager;
    private Uri basePath;
    private Gson gson;
    private OkHttpClient httpClient;

    public BasePubcrawlService(Uri basePath,
                               ConnectivityManager connectivityManager,
                               Gson gson,
                               OkHttpClient httpClient){
        this.connectivityManager = connectivityManager;
        this.basePath = basePath;
        this.gson = gson;
        this.httpClient = httpClient;
    }

    /**
     * Execute a basic HTTP Get request.
     */
    private Response performRequest(Uri uri) throws IOException {
        Timber.d("HTTP GET: " + uri.toString());
        Request request = new Request.Builder()
                .url(uri.toString())
                .build();
        return httpClient.newCall(request).execute();
    }

    private Uri getEditionPath(Edition edition){
        return Uri.withAppendedPath(basePath, edition.getPath());
    }

    /**
     * Return the catalog address. ".../editionCode/catalogFilename"
     */
    private Uri getCatalogUri(Edition edition) {
        // appends edition, i.e., /usa or /europe
        Uri editionPath = getEditionPath(edition);

        int catalogVersion = 1;

        if (edition == Edition.USA) {
            catalogVersion = 2;
        }
        String catalogPath = String.format(CATALOG_PATH, catalogVersion);

        return Uri.withAppendedPath(editionPath, catalogPath);
    }


    public Uri getBaseIssuePath(Edition edition, Issue issue){
        String contentPath = String.format("contents/%s", issue.getIssueId());
        return Uri.withAppendedPath(getEditionPath(edition), contentPath);
    }

    /**
     * Get Uri for file at issue's location.
     * @return remote file location
     */
    public Uri getUriFromIssue(Edition edition, Issue issue, String filename){
        return Uri.withAppendedPath(getBaseIssuePath(edition, issue), filename);
    }

    private Uri getIssueAddress(Edition edition, Issue issue){
        return Uri.withAppendedPath(getBaseIssuePath(edition, issue), "issue.json");
    }

    private Uri getSectionAddress(Edition edition, Issue issue, Section section){
        return Uri.withAppendedPath(getBaseIssuePath(edition, issue), section.getPagePath());
    }


    @Override
    public Observable<Catalog> getCatalogObservable(Edition edition) {
        return getCatalogObservable(edition, true);
    }

    @Override
    public Observable<Catalog> getCatalogObservable(final Edition edition, final boolean useCache) {
        final Uri catalogAddress = getCatalogUri(edition);

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

                    Reader responseStream = performRequest(catalogAddress).body().charStream();
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

    @Override
    public Observable<Issue> populateIssueWithSections(final Edition edition, final Issue issue) {
        Timber.d("Filling sections for issue: " + issue);

        return Observable.create(new Observable.OnSubscribe<Issue>() {

            @Override
            public void call(final Subscriber<? super Issue> subscriber) {
                try {
                    fillIssueSections(edition, issue);

                    subscriber.onNext(issue);
                    // save issue with sections
                    for (Section section : issue.getSections()){
                        section.saveWithKey(issue.getId());
                    }
                    subscriber.onCompleted();
                } catch (Exception exception) {
                    subscriber.onError(exception);
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    private Issue fillIssueSections(final Edition edition, final Issue issue) throws Exception {
        Uri address = getIssueAddress(edition, issue);

        List<Section> sections;

//        Check if sections already exist
        if (issue.getId() != null){
            sections = Section.findByKey(Section.class, issue.getId());
            if (sections != null && sections.size() > 0){
                Timber.i("(Cache) Obtained sections");
                issue.setSections(sections);
                return issue;
            }
        }

        Timber.i("(Network) Fetching sections for issue: " + issue);


        Reader json = performRequest(address).body().charStream();
        JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
        JsonObject jsonObj = jsonElement.getAsJsonObject();

        JsonArray jsonSections = jsonObj
                .getAsJsonArray("sections").get(0)
                .getAsJsonObject()
                .getAsJsonArray("items");

        sections = new LinkedList<>();

        for (JsonElement jsonSection : jsonSections) {
            Section section = gson.fromJson(jsonSection, Section.class);
            sections.add(section);
            section.saveWithKey(issue.getId());
        }
        issue.setSections(sections);
        return issue;
    }


    @Override
    public Observable<Section> populateSectionWithArticles(final Edition edition,
                                                           final Issue issue,
                                                           final Section section) {
        return Observable.create(new Observable.OnSubscribe<Section>() {

            @Override
            public void call(Subscriber<? super Section> subscriber) {
                Uri address = getSectionAddress(edition, issue, section);

                try {
                    List<Article> cachedArticles = Article.findByKey(Article.class, section.getId());
                    if (cachedArticles != null && cachedArticles.size() > 0){
                        Timber.d("(Cache) Obtained articles");
                        section.setArticles(cachedArticles);
                        subscriber.onNext(section);
                        subscriber.onCompleted();
                        return;
                    }
                    Timber.d("(Network) Fetching articles");
                    // we could do a request to see if content changed, and update accordingly
//                    String xml = DataModule.fetchUri(httpClient, url);
                    Timber.d(address.toString());
                    String xml = performRequest(address).body().string();
                    Timber.d(xml.substring(0, 20));
                    Article.ArticleListParser articleParser = new Article.ArticleListParser();
                    List<Article> articles = articleParser.parse(xml);
                    section.setArticles(articles);
                    subscriber.onNext(section);

                    for (Article article : articles){
                        article.saveWithKey(section.getId());
                    }
                    subscriber.onCompleted();

                } catch (Exception exception) {
                    Timber.e(exception.toString());
                    subscriber.onNext(section);
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
