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

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

/**
 * The GelcapService contains predefined network operations for use in Activities.
 *
 * ALTERNATIVE NAME: NetworkManager
 */
@Singleton
public class GelcapService {
    private static final String GELCAP_HOST = "gelcap.dowjones.com";
    private static final String CATALOG_PATH = "/gc/packager/wsj/us/android.phone.wifi.2.catalog.json";
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


    public Request getIssue(final IssueWrapper issueRef, final Listener<Issue> issueListener,
                            final Response.ErrorListener errorListener) {
        String url = "http://" + GELCAP_HOST + PRE_PATH + "/contents/" + issueRef.getIssueId() + "/issue.json";
        Timber.i("New issue request made: " + url);
        Request<Issue> request = new GsonRequest<Issue>(url, Issue.class, gson, issueListener, errorListener);
        return mRequestQueue.add(request);
    }

    public Request getSectionPage(final Section section, final Listener<SectionPage> sectionListener,
                                  final Response.ErrorListener errorListener) {
        String url = "http://gelcap.dowjones.com/gc/packager/wsj/europe/contents/NOW201411240210/FRONT_SECTION-pages.xml";
        Request<SectionPage> request = new XMLRequest<SectionPage>(url, SectionPage.class, sectionListener, errorListener);
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
