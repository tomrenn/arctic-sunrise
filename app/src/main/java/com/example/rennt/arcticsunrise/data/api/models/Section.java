package com.example.rennt.arcticsunrise.data.api.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * Created by rennt on 11/10/14.
 */
public class Section implements Parcelable{
    private String name;
    private String title;
    @SerializedName("pages")
    private String page;
    @SerializedName("hidden")
    private boolean isHidden;
    @SerializedName("paid")
    private boolean isPaid;
    private String contentUrl;

    public Section(Parcel in){
        name = in.readString();
        title = in.readString();
        contentUrl = in.readString();
        boolean[] bools = new boolean[2];
        in.readBooleanArray(bools);
        isHidden = bools[0];
        isPaid = bools[1];
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
}
