package com.example.rennt.articsunrise;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

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
