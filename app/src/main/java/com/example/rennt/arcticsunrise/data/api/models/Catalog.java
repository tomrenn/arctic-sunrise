package com.example.rennt.arcticsunrise.data.api.models;

import com.orm.SugarRecord;

import java.util.List;

/**
 * Created by rennt on 11/9/14.
 */
public class Catalog extends SugarRecord<Catalog>{
    private int version;
    private List<Issue> items;

    public List<Issue> getIssues(){
        return this.items;
    }
}
