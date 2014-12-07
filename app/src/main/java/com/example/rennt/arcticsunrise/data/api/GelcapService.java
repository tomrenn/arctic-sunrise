package com.example.rennt.arcticsunrise.data.api;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.IssueWrapper;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.data.api.requests.GsonRequest;
import com.example.rennt.arcticsunrise.data.api.requests.XMLRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
        this.gson = new GsonBuilder()
                .registerTypeAdapter(Issue.class, new Issue.IssueDeserializer())
                .create();
    }

    public RequestQueue getRequestQueue() {
        return mRequestQueue;
    }

    public ImageLoader getImageLoader() {
        return mImageLoader;
    }

    public Observable<Issue> getIssueObservable(final IssueWrapper issueRef) {
        return Observable.create(new Observable.OnSubscribe<Issue>() {
            @Override
            public void call(final Subscriber<? super Issue> subscriber) {

                // perform the volley request
                getIssue(issueRef, new Listener<Issue>() {
                    @Override
                    public void onResponse(Issue response) {
                        subscriber.onNext(response);
                        subscriber.onCompleted();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        subscriber.onError(error);
                        subscriber.onCompleted();
                    }
                });
            }
        }).subscribeOn(Schedulers.io())
          .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Request the issue object based on an IssueWrapper
     */
    public Request getIssue(final IssueWrapper issueRef, final Listener<Issue> issueListener,
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
        String url = "http://gelcap.dowjones.com/gc/packager/wsj/europe/contents/NOW201411240210/FRONT_SECTION-pages.xml";
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
        private static String NEW_DOMAIN = "gelcap.com";
        private static String OLD_FORMAT = "http://%s/gc/packager/%s";
        private static String OLD_DOMAIN = "mitp.wsj.com";

        private static String CATALOG_PATH = "/android.phone.wifi.%d.catalog.json";
        private static String ISSUE_BASE = "/contents/%s";
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

        /**
         * Return base location for issue information.
         */
        private static String getBaseIssuePath(Edition edition, IssueWrapper issueWrapper){
            String path = getBasePath(edition) + ISSUE_BASE;
            return String.format(path, issueWrapper.getIssueId());
        }

        public static String getIssueAddress(Edition edition, IssueWrapper issueWrapper){
            return getBaseIssuePath(edition, issueWrapper) + "/issue.json";
        }

        public static String getSectionAddress(Edition edition, IssueWrapper issueWrapper, Section section){
            return getBaseIssuePath(edition, issueWrapper) + "/" + section.getPagePath();
        }

    }

}
