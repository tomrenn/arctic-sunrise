package com.example.rennt.arcticsunrise.data.api.models;

import java.util.List;

/**
 * Created by rennt on 11/9/14.
 */
public class Catalog {
    private int version;
    private List<IssueWrapper> items;

    public List<IssueWrapper> getIssues(){
        return this.items;
    }
}
