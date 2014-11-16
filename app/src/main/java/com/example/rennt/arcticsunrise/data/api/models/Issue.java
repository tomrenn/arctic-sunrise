package com.example.rennt.arcticsunrise.data.api.models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by rennt on 11/10/14.
 */
public class Issue {
    @SerializedName("date_updated")
    private Date dateUpdated;
    @SerializedName("schema_version")
    private String schemaVersion;
    private List<Section> sections;

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public void setSections(List<Section> sections) {
        this.sections = sections;
    }

    /**
     * Adapter to perform gson deserialization.
     * issue["sections"] do not map between the gelcap representation and
     * the data models represented here.
     */
    public static class IssueDeserializer implements JsonDeserializer<Issue> {
        public Issue deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {

            Issue issue = new Issue();
            JsonObject jsonObj = json.getAsJsonObject();

            try{
                issue.setDateUpdated(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(
                        jsonObj.get("date_updated").getAsString()));
                issue.setSchemaVersion(jsonObj.get("schema_version").getAsString());

                JsonArray jsonSections = jsonObj
                        .getAsJsonArray("sections").get(0)
                        .getAsJsonObject()
                        .getAsJsonArray("items");

                List<Section> sectionList = new LinkedList<Section>();
                Iterator<JsonElement> itty = jsonSections.iterator();
                Gson gson = new Gson();

                while (itty.hasNext()){
                    sectionList.add(gson.fromJson(itty.next(), Section.class));
                }
                issue.setSections(sectionList);
            }
            catch (ParseException e){
                throw new JsonParseException(e.toString());
            }


            return issue;
        }
    }


    public Date getDateUpdated() {
        return dateUpdated;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public List<Section> getSections() {
        return sections;
    }
}
