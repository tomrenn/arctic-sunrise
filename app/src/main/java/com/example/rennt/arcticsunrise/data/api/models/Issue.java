package com.example.rennt.arcticsunrise.data.api.models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Represents the Issue representation that exists in the Catalog endpoint.
 *
 * Created by rennt on 11/8/14.
 */
public class Issue extends SugarRecord<Issue>{
    private String type;
    private String key;
    private String first;
    private String label;
    private String manifest;

    // Filled by a second request
    @Expose
    private List<Section> sections;
    @Expose @SerializedName("schema_version")
    private String schemaVersion;

    public String getKey(){
        return key;
    }


    public void setSections(List<Section> sections){
        this.sections = sections;
    }

    public String getIssueId(){
        return this.first.split("/")[0];
    }

    public List<Section> getSections(){ return this.sections; }

    public String getIssueAddress(){
        return "";
    }

}
