package com.example.rennt.arcticsunrise.data.api.models;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rennt on 11/10/14.
 */
@Root(name="Pages", strict=false)
public class SectionPage {
    @ElementList(name="link")
    private List<Article> articles;

    public List<Article> getArticles(){
        return articles;
    }
}
