package com.example.rennt.arcticsunrise.data;

import com.example.rennt.arcticsunrise.data.api.CatalogService;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.IssueService;
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
public class MockPubcrawlService implements CatalogService, IssueService {

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


    public List<Section> getSections(Issue issue){
        List<Section> sections = new ArrayList<>();
        sections.add(new Section.Builder()
                        .setName("BEST_SECTION")
                        .setTitle("TEH VRY BEST")
                        .build());
        return sections;
    }

    public List<Article> getArticles(Section section){
        List<Article> articles = new ArrayList<>();
        articles.add(simpleArticle("Dragons attack", "Hide yo kids, hide yo dragonborn"));

        return articles;
    }

    private Article simpleArticle(String headline, String summary){
        return getArticle(headline, summary, false, true, null);
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
    public Observable<Catalog> getCatalogObservable() {
        return getCatalogObservable(false);
    }

    @Override
    public Observable<Catalog> getCatalogObservable(boolean useCache) {
        return Observable.just(getCatalog(Edition.USA));
    }

    @Override
    public Observable<Issue> getIssueSectionsObservable() {
        Issue mockIssue = mockIssue1();
        mockIssue.setSections(getSections(mockIssue));
        return Observable.just(mockIssue);
    }

    @Override
    public Observable<Section> buildSectionArticlesObservable(int sectionPos) {
        List<Article> articles = getArticles(null);
        Section section = getSections(null).get(sectionPos);
        section.setArticles(articles);
        return Observable.just(section);
    }
}
