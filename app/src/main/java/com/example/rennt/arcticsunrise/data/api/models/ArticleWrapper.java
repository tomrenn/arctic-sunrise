package com.example.rennt.arcticsunrise.data.api.models;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Path;
import org.simpleframework.xml.Root;

/**
* Summary of an article
*/
public class ArticleWrapper {

    // use xpath to get difficult things
    @Element
    @Path("metadata/item[@key=headline]")
    private String headline;
//    private String type;
    @Element
    @Path("metadata/item[@key=paid]")
    private boolean isPaid;
    @Element
    @Path("metadata/item[@key=summary]")
    private String summmary;
//    private String thumbnail;
    @Element
    @Attribute(name="metadata/item[@key=short_url]")
    private String articleSource;
    // Item key="share_link"
//    private String articleLink;

}
