package com.example.rennt.arcticsunrise.data.api.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.rennt.arcticsunrise.data.api.SugarKeyRecord;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.List;

/**
 * Created by rennt on 11/10/14.
 */
public class Section extends SugarKeyRecord<Section> implements Parcelable{
    private String name;
    private String title;
    @SerializedName("pages")
    private String page;
    @SerializedName("hidden")
    private boolean isHidden;
    @SerializedName("paid")
    private boolean isPaid;
    private String contentUrl;

    // derived
    @Expose @Ignore
    private List<Article> articles;

    /**
     * Default constructor has to stick around for SugarRecord purposes.
     * https://github.com/satyan/sugar/issues/50
     */
    public Section(){
        super();
    }

    private Section(final String name, final String title){
        this.name = name;
        this.title = title;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getPagePath() { return getName() + "-pages.xml"; }

    public boolean isHidden() {
        return isHidden;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(title);
        dest.writeString(contentUrl);
        dest.writeBooleanArray(new boolean[] {isHidden, isPaid});
    }

    public List<Article> getArticles() {
        return articles;
    }

    public List<Article> lookupArticleSet(){
        return Article.find(Article.class, "_section = ?", this.getId().toString());
    }

    public void setArticles(List<Article> articles) {
        this.articles = articles;
    }

    public static class Builder {
        private String name;
        private String title;
        private List<Section> sections;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Section build(){
            return new Section(name, title);
        }
    }

}
