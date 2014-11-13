package com.example.rennt.articsunrise;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rennt on 11/10/14.
 */
public class Section {
    private String name;
    private String title;
    private String pages;
    @SerializedName("hidden")
    private boolean isHidden;
    @SerializedName("paid")
    private boolean isPaid;


    public boolean isPaid() {
        return isPaid;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHidden() {
        return isHidden;
    }



}
