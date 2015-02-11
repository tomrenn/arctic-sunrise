package com.example.rennt.arcticsunrise.data;

import android.app.Application;
import android.content.Context;
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
    private Context context;

    public MockPubcrawlService(Application app){
        this.context = app;
    }

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

    private String getMockImage(String imageName){
        return "file:///android_asset/mock/images/" + imageName;
//        return this.context.getAssets().open();
        //"mock/images/" + imageName);
    }

    private List<Section> getSections(){
        List<Section> sections = new ArrayList<>();
        sections.add(new Section.Builder()
                .setName("BEST_SECTION")
                .setTitle("TEH VRY BEST")
                .build());

        sections.add(new Section.Builder()
                        .setName("mock2")
                        .setTitle("Such Mock")
                        .build());
        return sections;
    }

    public List<Article> getArticles(Section section){
        List<Article> articles = new ArrayList<>();

        if (section.getName().equals("BEST_SECTION")) {
            articles.add(getArticle(
                    "Lollipop, awww yis. Dis a deco", "", true, false, getMockImage("lollipop.png")));

            articles.add(getArticle(
                    "Power of open source",
                    "There are many appeals of open source software - security, wide adoption, and " +
                            "support.", false, false, getMockImage("opensource.png")));

            articles.add(simpleArticle("New AI set to run for office",
                    "Sets record campaign funding through bitcoin due to botnet support"));

            articles.add(getArticle(
                    "World renown artists", "A look at the past and present.", false, true,
                    getMockImage("starry.png")));

        } else if (section.getName().equals("mock2")) {
            articles.add(getArticle(
                    "Maintaining peak performance.", "", true, false, getMockImage("programmingskill.png")));

            articles.add(getArticle(
                    "Secret Nikola Telsa notes found",
                    "Energy companies scramble to provide wireless power",
                    false, false,
                    getMockImage("tesla.png")));
        }



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
        List<Article> articles = getArticles(section);
        section.setArticles(articles);
        return Observable.just(section);
    }
}
