package com.example.rennt.articsunrise;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by rennt on 11/8/14.
 */
public interface GelcapService {

    @GET("/gc/packager/wsj/us/android.phone.wifi.2.catalog.json")
    void getCatalog(Callback<Catalog> cb);


    @GET("/gc/packager/wsj/us/contents/{issueId}/issue.json")
    void getIssue(@Path("issueId") String issueId, Callback<Issue> cb);
}
