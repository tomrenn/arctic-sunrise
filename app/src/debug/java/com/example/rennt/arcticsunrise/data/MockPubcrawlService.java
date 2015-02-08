package com.example.rennt.arcticsunrise.data;

import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tomrenn on 2/7/15.
 */
public class MockPubcrawlService {

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
}
