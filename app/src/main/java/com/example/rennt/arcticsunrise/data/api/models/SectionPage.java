package com.example.rennt.arcticsunrise.data.api.models;

import com.google.gson.annotations.SerializedName;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by rennt on 11/10/14.
 */
@Root(name="Pages", strict=false)
public class SectionPage {
    @ElementList(name="link")
    private List<ArticleWrapper> articles;

    public List<ArticleWrapper> getArticles(){
        return articles;
    }
}
