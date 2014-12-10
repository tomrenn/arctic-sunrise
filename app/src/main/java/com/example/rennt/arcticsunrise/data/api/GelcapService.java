package com.example.rennt.arcticsunrise.data.api;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
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

import java.io.UnsupportedEncodingException;
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
    private final Gson gson;

    /**
     * We can use constructor injection here because Dagger, not the Android OS,
     * will be doing the construction of this object.
     */
    @Inject
    public GelcapService(Edition edition, RequestQueue queue, ImageLoader imgLoader){
        this.edition = edition;
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


    /**
     * Subscribe to retrieve catalog objects.
     */
    public Observable<Catalog> getCatalogObservable() {
        return Observable.create(new Observable.OnSubscribe<Catalog>(){
            @Override
            public void call(final Subscriber<? super Catalog> subscriber) {

                // perform volley request
                getCatalog(new Listener<Catalog>() {
                    @Override
                    public void onResponse(Catalog response) {
                        subscriber.onNext(response);
                        subscriber.onCompleted();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error){
                        subscriber.onError(error);
                        subscriber.onCompleted();
                    }
                });
            }
         }).subscribeOn(Schedulers.io())
           .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Given the issue, request and set its list of sections
     */
    public Observable<Issue> getIssueSectionsObservable(final Issue issue) {
        return Observable.create(new Observable.OnSubscribe<Issue>() {
            @Override
            public void call(final Subscriber<? super Issue> subscriber) {

                // volley request
                fillIssueSections(issue, new Listener<Issue>(){
                    @Override
                    public void onResponse(Issue issue){
                        subscriber.onNext(issue);
                        subscriber.onCompleted();
                    }

                }, null);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


    public Request fillIssueSections(final Issue issue, final Listener<Issue> issueListener,
                                     final Response.ErrorListener errorListener){
        String address = NetworkResolver.getIssueAddress(edition, issue);
        Timber.i("Filling issue sections of issue: " + issue);
        Request<Issue> request = new Request<Issue>(Request.Method.GET, address, errorListener) {
            @Override
            protected Response<Issue> parseNetworkResponse(NetworkResponse response) {
                try {
                    String json = new String(response.data, "UTF-8");
                    JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
                    JsonObject jsonObj = jsonElement.getAsJsonObject();
                    //getAsJsonObject();
//                    GsonBuilder builder = new GsonBuilder().
//                    JsonObject jsonObj = json.getAsJsonObject();

//                    issue.setDateUpdated(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(
//                            jsonObj.get("date_updated").getAsString()));
//                    issue.setSchemaVersion(jsonObj.get("schema_version").getAsString());

                    JsonArray jsonSections = jsonObj
                            .getAsJsonArray("sections").get(0)
                            .getAsJsonObject()
                            .getAsJsonArray("items");

                    List<Section> sectionList = new LinkedList<Section>();
                    Iterator<JsonElement> itty = jsonSections.iterator();
                    Gson gson = new Gson();

                    while (itty.hasNext()){
                        sectionList.add(gson.fromJson(itty.next(), Section.class));
                    }
                    issue.setSections(sectionList);
                }
                catch (UnsupportedEncodingException e){
                    throw new JsonParseException(e.toString());
                }
//                catch (ParseException e){
//                    throw new JsonParseException(e.toString());
//                }
                return Response.success(issue, HttpHeaderParser.parseCacheHeaders(response));
            }

            @Override
            protected void deliverResponse(Issue response) {
                issueListener.onResponse(response);
            }
        };
        return mRequestQueue.add(request);
    }


    /**
     * Request the issue object based on an IssueWrapper
     */
    public Request getIssue(final Issue issueRef, final Listener<Issue> issueListener,
                            final Response.ErrorListener errorListener) {
        String address = NetworkResolver.getIssueAddress(edition, issueRef);
        Timber.i("Issue request (network): " + address);

        Request<Issue> request = new GsonRequest<Issue>(address, Issue.class, gson, issueListener, errorListener);
        // todo: fill issue sections with the sectionUrl
        // todo: frick, add this feature to Issue Deserializer
        return mRequestQueue.add(request);
    }


    public Request fillSectionArticles(final Issue issue, final int section){
        throw new UnsupportedOperationException();
    }


    /**
     * Make a request for the content (articles) in a section.
     */
    public Request getSectionContent(final Section section, final Listener<List<Article>> sectionListener,
                                  final Response.ErrorListener errorListener) {
//        NetworkResolver.getSectionAddress(edition, null, section);
        String url = "http://gelcap.dowjones.com/gc/packager/wsj/us/contents/NOW201412100010/";
        url = url + section.getPagePath();
        XMLRequest.XMLParser articleListParser = new Article.ArticleListParser();
        Request<List<Article>> request = new XMLRequest<List<Article>>(url, articleListParser,
                sectionListener, errorListener);
        return mRequestQueue.add(request);
    }

    /**
     * Pass in a catalog listener to receive a freshly parsed catalog from the network.
     */
    public Request getCatalog(final Listener<Catalog> catalogListener,
                           final Response.ErrorListener errorListener){
        String address = NetworkResolver.getCatalogAddress(edition);

        Timber.d("Catalog request (network): " + address);
        Request<Catalog> request = new GsonRequest<Catalog>(address, Catalog.class, gson,
                                                            catalogListener, errorListener);
        return mRequestQueue.add(request);
    }


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
