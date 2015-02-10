package com.example.rennt.arcticsunrise.data;

import android.net.Uri;

import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.PubcrawlService;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tomrenn on 2/7/15.
 */
public class MockPubcrawlService implements PubcrawlService {

    public Catalog getCatalog(Edition edition){
        return new Catalog.Builder()
                .setVersion(9001)
                .addIssue(mockIssue1())
                .build();
    }

    private Issue mockIssue1(){
        return new Issue.Builder()
                .setKey("NOW")
                .setType("NOW")
                .build();
    }


    public List<Section> getSections(){
        List<Section> sections = new ArrayList<>();
        sections.add(new Section.Builder()
                        .setName("BEST_SECTION")
                        .setTitle("TEH VRY BEST")
                        .build());
        return sections;
    }

    public List<Article> getArticles(Section section){
        List<Article> articles = new ArrayList<>();
        articles.add(simpleArticle("New AI set to run for office",
                "Declares there is nothing to worry."));

        return articles;
    }

    private Article simpleArticle(String headline, String summary){
        return getArticle(headline, summary, false, true, "");
    }

    private Article getArticle(String headline, String summary, boolean isDeco, boolean isPaid,
                               String thumbnail){
        return new Article.Builder()
                .setHeadline(headline)
                .setSummary(summary)
                .setDeco(isDeco)
                .setPaid(isPaid)
                .setThumbnail(thumbnail)
                .build();
    }


    @Override
    public Observable<Catalog> getCatalogObservable(Edition edition) {
        return getCatalogObservable(edition, true);
    }

    @Override
    public Observable<Catalog> getCatalogObservable(Edition edition, boolean useCache) {
        return Observable.just(getCatalog(Edition.USA));
    }

    @Override
    public Uri getUriFromIssue(Edition edition, Issue issue, String filename) {
        return new Uri.Builder().scheme("http").build();
    }

    @Override
    public Observable<Issue> populateIssueWithSections(Edition edition, Issue issue) {
        issue.setSections(getSections());
        return Observable.just(issue);
    }

    @Override
    public Observable<Section> populateSectionWithArticles(Edition edition, Issue issue, Section section) {
        List<Article> articles = getArticles(null);
        section.setArticles(articles);
        return Observable.just(section);
    }
}
