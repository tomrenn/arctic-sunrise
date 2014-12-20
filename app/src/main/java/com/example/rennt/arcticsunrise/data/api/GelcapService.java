package com.example.rennt.arcticsunrise.data.api;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.data.api.requests.GsonRequest;
import com.example.rennt.arcticsunrise.data.api.requests.XMLRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * The GelcapService contains predefined network operations for use in Activities.
 *
 *
 * DebugVersion: MockGelcapService (provides)
 *
 * Interface
 * - getCatalogObservable()
 * - getFillIssueObservable(Issue i)
 * - getArticlePageObservable(Section s)  : get List of Articles in Section
 * -
 *
 * ALTERNATIVE NAME: NetworkManager
 */
@Singleton
public class GelcapService {
    private static final String GELCAP_HOST = "gelcap.dowjones.com";
    private static final String CATALOG_PATH = "/android.phone.wifi.2.catalog.json";
    // String.format("%s");
//    private static final String TEST = String.format(Locale.US, "/android.phone.wifi.%s.catalog.json", "2");
    private static final String PRE_PATH = "/gc/packager/wsj/us";
    private final Edition edition;
    private final RequestQueue mRequestQueue;
    private final ImageLoader mImageLoader;
    private final OkHttpClient httpClient;
    private final Gson gson;

    /**
     * We can use constructor injection here because Dagger, not the Android OS,
     * will be doing the construction of this object.
     */
    @Inject
    public GelcapService(Edition edition, RequestQueue queue, ImageLoader imgLoader,
                         OkHttpClient httpClient){
        this.edition = edition;

        this.httpClient = httpClient;
        this.mRequestQueue = queue;
        mRequestQueue.start();
        this.mImageLoader = imgLoader;
        // custom gson with special deserializers
        this.gson = new GsonBuilder().create();
    }



    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }


    private String fetchURL(String url) throws IOException{
        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = httpClient.newCall(request).execute();
        return response.body().string();
    }

    /**
     * Subscribe to retrieve catalog objects.
     */
    public Observable<Catalog> getCatalogObservable() {
        return Observable.create(new Observable.OnSubscribe<Catalog>(){
            @Override
            public void call(final Subscriber<? super Catalog> subscriber) {

                String address = NetworkResolver.getCatalogAddress(edition);
                try {
                    Catalog catalog;
                    Iterator<Catalog> savedCatalogs = Catalog.findAll(Catalog.class);
                    if (savedCatalogs.hasNext()){
                        catalog = savedCatalogs.next();
                        Timber.d("(Cache) Obtained Catalog");
                        // lookup cached issues
                        List<Issue> issueSet = catalog.lookupIssueSet();
                        Field _issues = Catalog.class.getDeclaredField("issues");
                        _issues.setAccessible(true);
                        _issues.set(catalog, issueSet);

                        subscriber.onNext(catalog);
                        subscriber.onCompleted();
                        return;
                    }
                    Timber.d("Catalog request (network): " + address);
//                    else {
                        String response = fetchURL(address);
                        catalog = gson.fromJson(response, Catalog.class);

                        // set the private variable in the issue
                        // todo: or we create a custom deserialization that uses a builder..
                        Field _catalogField = Issue.class.getDeclaredField("_catalog");
                        _catalogField.setAccessible(true);
                        for (Issue issue : catalog.getIssues()){
                            _catalogField.set(issue, catalog);
                        }


                        subscriber.onNext(catalog);
                        Timber.d("After catalog onNext");

                        // save to database for future use
                        catalog.save();
                        for (Issue issue: catalog.getIssues()){
                            issue.save();
                        }

                        Timber.d("Saved issues");
//                    }
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

    /**
     * Given the issue, request and set its list of sections
     */
    public Observable<Issue> getIssueSectionsObservable(final Issue issue) {
        Timber.d("Filling sections for issue: " + issue);

        return Observable.create(new Observable.OnSubscribe<Issue>() {

            @Override
            public void call(final Subscriber<? super Issue> subscriber) {
                try {
                    fillIssueSections(issue);

                    subscriber.onNext(issue);
                    // save issue with sections
                    issue.save();
                    for (Section section : issue.getSections()){
                        section.save();
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


    /**
     * Find and set the sections for the given issue.
     *
     * @param issue
     * @return
     * @throws IOException
     */
    private Issue fillIssueSections(final Issue issue) throws Exception {
        String address = NetworkResolver.getIssueAddress(edition, issue);

        List<Section> sections;

//        Check if sections already exist
        if (issue.getId() != null){
            sections = Section.find(Section.class, "_issue = ?", issue.getId().toString());
            if (sections != null && sections.size() > 0){
                Timber.i("(Cache) Obtained sections");
                issue.setSections(sections);
                return issue;
            }
        }

        Timber.i("(Network) Fetching sections for issue: " + issue);


        String json = fetchURL(address);
        JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
        JsonObject jsonObj = jsonElement.getAsJsonObject();

        JsonArray jsonSections = jsonObj
                .getAsJsonArray("sections").get(0)
                .getAsJsonObject()
                .getAsJsonArray("items");

        sections = new LinkedList<>();

        // set the private variable in the issue
        // todo: or we create a custom deserialization that uses a builder..
        Field _issueField = Section.class.getDeclaredField("_issue");
        _issueField.setAccessible(true);


        for (Iterator<JsonElement> itty = jsonSections.iterator(); itty.hasNext();) {
            Section section = gson.fromJson(itty.next(), Section.class);
            _issueField.set(section, issue);
            sections.add(section);
        }
        issue.setSections(sections);
        return issue;
    }

    /**
     * Return an Observable that fills the given section with the list of articles it contains.
     */
    public Observable<Section> buildSectionArticlesObservable(final Section section){
        return Observable.create(new Observable.OnSubscribe<Section>() {

            @Override
            public void call(Subscriber<? super Section> subscriber) {
                // FIXME: use the correct url address..
                String url = "http://gelcap.dowjones.com/gc/packager/wsj/us/contents/NOW201412200320/";
                url = url + section.getPagePath();

                try {
                    // lookup cached articles
                    List<Article> cachedArticles = section.lookupArticleSet();
                    if (cachedArticles != null && cachedArticles.size() > 0){
                        Timber.d("(Cache) Obtained articles");
                        section.setArticles(cachedArticles);
                        subscriber.onNext(section);
                        subscriber.onCompleted();
                        return;
                    }
                    Timber.d("(Network) Fetching articles");
                    // we could do a request to see if content changed, and update accordingly
                    String xml = fetchURL(url);

                    Article.ArticleListParser articleParser = new Article.ArticleListParser(section);
                    List<Article> articles = articleParser.parse(xml);
                    section.setArticles(articles);
                    subscriber.onNext(section);

                    for (Article article : articles){
                        article.save();
                    }
                    subscriber.onCompleted();

                } catch (Exception exception) {
                    subscriber.onNext(section);
                    subscriber.onCompleted();
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

//    /**
//     * Make a request for the content (articles) in a section.
//     */
//    public com.android.volley.Request getSectionContent(final Section section, final Listener<List<Article>> sectionListener,
//                                  final com.android.volley.Response.ErrorListener errorListener) {
////        NetworkResolver.getSectionAddress(edition, null, section);
//        String url = "http://gelcap.dowjones.com/gc/packager/wsj/us/contents/NOW201412100010/";
//        url = url + section.getPagePath();
//        XMLRequest.XMLParser articleListParser = new Article.ArticleListParser();
//        com.android.volley.Request<List<Article>> request = new XMLRequest<List<Article>>(url, articleListParser,
//                sectionListener, errorListener);
//        return mRequestQueue.add(request);
//    }



    /**
     * Resolve gelcap paths, since it is not always straight-forward..
     */
    static class NetworkResolver {
        //http://gelcap.dowjones.com/gc/packager/wsj/us/android.phone.wifi.2.catalog.json
        // host / edition / version number
        private static String NEW_FORMAT = "http://%s/gc/packager/wsj/%s";
        private static String NEW_DOMAIN = "gelcap.dowjones.com";
        private static String OLD_FORMAT = "http://%s/gc/packager/%s";
        private static String OLD_DOMAIN = "mitp.wsj.com";

        private static String CATALOG_PATH = "/android.phone.wifi.%d.catalog.json";
        private static String ISSUE_BASE = "/contents/%s";

        // todo: future StringPreference for debug/mock address/endpoints!

        /**
         * Determine the root url location based on edition.
         */
        private static String getBasePath(Edition edition){
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
        public static String getCatalogAddress(Edition edition){
            String base = getBasePath(edition);
            int catalogVersion = 1;

            if (edition == Edition.USA) {
                catalogVersion = 2;
            }
            String catalogPath = String.format(CATALOG_PATH, catalogVersion);

            return base + catalogPath;
        }

//        public static String getSectionAddress(Edition )

        /**
         * Return base location for issue information.
         */
        private static String getBaseIssuePath(Edition edition, Issue issue){
            String path = getBasePath(edition) + ISSUE_BASE;
            return String.format(path, issue.getIssueId());
        }

        public static String getIssueAddress(Edition edition, Issue issue){
            return getBaseIssuePath(edition, issue) + "/issue.json";
        }

        public static String getSectionAddress(Edition edition, Issue issue, Section section){
            return getBaseIssuePath(edition, issue) + "/" + section.getPagePath();
        }

    }

}
