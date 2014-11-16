package com.example.rennt.arcticsunrise.data.api.models;

/**
 * Represents the Issue representation that exists in the Catalog endpoint.
 *
 * Created by rennt on 11/8/14.
 */
public class IssueWrapper {
    private String type;
    private String key;
    private String first;
    private String label;
    private String manifest;

    public String getKey(){
        return key;
    }


    public String getIssueId(){
        return this.first.split("/")[0];
    }


    public String getIssueAddress(){
        return "";
    }
}
