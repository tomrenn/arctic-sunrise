package com.example.rennt.arcticsunrise.data.api.models;

import com.example.rennt.arcticsunrise.data.api.Edition;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.List;

/**
 * Created by rennt on 11/9/14.
 */
public class Catalog extends SugarRecord<Catalog>{
    private int version;
    @SerializedName("items") @Ignore
    private List<Issue> issues;

    @Expose
    private Edition _edition;

    public List<Issue> getIssues(){
        return this.issues;
    }

    /**
     * Lookup <related> set. Could be an interface(?).
     * cached database query to find the related objects.
     *
     * @return
     */
    public List<Issue> lookupIssueSet(){
        return Issue.find(Issue.class, "_catalog = ?", this.getId().toString());
    }
}
