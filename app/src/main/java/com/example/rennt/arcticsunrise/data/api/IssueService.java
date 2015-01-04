package com.example.rennt.arcticsunrise.data.api;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.rennt.arcticsunrise.data.DataModule;
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
import java.io.Reader;
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

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * The IssueService contains predefined network operations for use in Activities.
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
public class IssueService {
    private OkHttpClient httpClient;
    private Issue issue;
    private String baseEditionPath;
    private Gson gson;
    /**
     * We can use constructor injection here because Dagger, not the Android OS,
     * will be doing the construction of this object.
     */

    public IssueService(OkHttpClient httpClient, Gson gson,
                        Issue issue, @BaseEditionPath String baseEditionPath){
        this.httpClient = httpClient;
        this.issue = issue;
        this.baseEditionPath = baseEditionPath;
        this.gson = gson;
    }

    public Issue getIssue(){
        return issue;
    }

    /**
     * Return base location for issue information.
     */
    private String getBaseIssuePath(){
        String path = baseEditionPath + "/contents/%s";
        return String.format(path, issue.getIssueId());
    }

    private String getIssueAddress(){
        return getBaseIssuePath() + "/issue.json";
    }

    private String getSectionAddress(Section section){
        return getBaseIssuePath() + "/" + section.getPagePath();
    }

    /**
     * Given the issue, request and set its list of sections
     */
    public Observable<Issue> getIssueSectionsObservable() {
        Timber.d("Filling sections for issue: " + issue);

        return Observable.create(new Observable.OnSubscribe<Issue>() {

            @Override
            public void call(final Subscriber<? super Issue> subscriber) {
                try {
                    fillIssueSections();

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


    /**
     * Find and set the sections for the given issue.
     *
     * @return
     * @throws java.io.IOException
     */
    @DebugLog
    private Issue fillIssueSections() throws Exception {
        String address = getIssueAddress();

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


        Reader json = DataModule.fetchUri(httpClient, address);
        JsonElement jsonElement = gson.fromJson(json, JsonElement.class);
        JsonObject jsonObj = jsonElement.getAsJsonObject();

        JsonArray jsonSections = jsonObj
                .getAsJsonArray("sections").get(0)
                .getAsJsonObject()
                .getAsJsonArray("items");

        sections = new LinkedList<>();

        for (Iterator<JsonElement> itty = jsonSections.iterator(); itty.hasNext();) {
            Section section = gson.fromJson(itty.next(), Section.class);
            sections.add(section);
            section.saveWithKey(issue.getId());
        }
        issue.setSections(sections);
        return issue;
    }

    /**
     * Return an Observable that fills the given section with the list of articles it contains.
     */
    public Observable<Section> buildSectionArticlesObservable(final int sectionPos){
        return Observable.create(new Observable.OnSubscribe<Section>() {

            @Override
            public void call(Subscriber<? super Section> subscriber) {
                Section section = issue.getSections().get(sectionPos);
                String url = getSectionAddress(section);

                try {
                    // FIXME: this is kinda a hack, add precondition that section must be saved before onNext'ing
                    Long sectionKey = section.getId();
                    while (sectionKey == null){
                        Timber.d("Section key does not exist yet");
                        Thread.sleep(50);
                    }

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
                    Timber.d(url);
                    String xml = DataModule.fetchUriToString(httpClient, url);
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
