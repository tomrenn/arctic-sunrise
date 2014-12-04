package com.example.rennt.arcticsunrise.data.api;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.IssueWrapper;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.data.api.models.SectionPage;
import com.example.rennt.arcticsunrise.data.api.requests.GsonRequest;
import com.example.rennt.arcticsunrise.data.api.requests.XMLRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.UnsupportedEncodingException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
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
    private final RequestQueue mRequestQueue;
    private final ImageLoader mImageLoader;
    private final Gson gson;

    /**
     * We can use constructor injection here because Dagger, not the Android OS,
     * will be doing the construction of this object.
     */
    @Inject
    public GelcapService(RequestQueue queue, ImageLoader imgLoader){
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
        String issueBase = "http://" + GELCAP_HOST + PRE_PATH + "/contents/" + issueRef.getIssueId();
        String issuePath = issueBase + "/issue.json";
        Timber.i("New issue request made: " + issuePath);
        Request<Issue> request = new GsonRequest<Issue>(issuePath, Issue.class, gson, issueListener, errorListener);
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
        String catalog_url = "http://" + GELCAP_HOST + CATALOG_PATH;
        Timber.i("New Catalog request made: " + catalog_url);
        Request<Catalog> request = new GsonRequest<Catalog>(catalog_url, Catalog.class, gson,
                                                            catalogListener, errorListener);
        return mRequestQueue.add(request);
    }


}
