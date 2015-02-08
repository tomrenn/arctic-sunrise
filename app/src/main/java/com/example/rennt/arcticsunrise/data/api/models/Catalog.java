package com.example.rennt.arcticsunrise.data.api.models;

import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.SugarKeyRecord;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rennt on 11/9/14.
 */
public class Catalog extends SugarKeyRecord<Catalog> {
    private int version;
    @SerializedName("items") @Ignore
    private List<Issue> issues;

    private Catalog(final int version, final List<Issue> issues){
        this.version = version;
        this.issues = issues;
    }


    public List<Issue> getIssues(){
        return this.issues;
    }

    /**
     * Return the matching issue if it exists in the catalog or null.
     */
    public Issue containsIssue(Issue givenIssue){
        for (Issue issue : issues){
            if (givenIssue.getKey().equals(issue.getKey()) &&
                    givenIssue.getRevision() == issue.getRevision()){
                return issue;
            }
        }
        return null;
    }

    public static class Builder {
        private int version;
        private List<Issue> issues = new ArrayList<>();

        public Builder setVersion(final int version){
            this.version = version;
            return this;
        }

        public Builder addIssue(final Issue issue){
            this.issues.add(issue);
            return this;
        }

        public Catalog build(){
            return new Catalog(version, issues);
        }
    }
}
