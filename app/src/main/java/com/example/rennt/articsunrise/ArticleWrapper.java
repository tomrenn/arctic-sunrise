package com.example.rennt.articsunrise;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * Summary of an article
 */
@Root(name="page")
public class ArticleWrapper {

    // use xpath to get difficult things

    private String headline;
    private String type;
    private boolean isPaid;
    private String summmary;
    private String thumbnail;
    @Attribute(name="url")
    private String articleSource;
    // Item key="share_link"
    private String articleLink;

}
