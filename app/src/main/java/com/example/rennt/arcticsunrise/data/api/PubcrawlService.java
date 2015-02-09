package com.example.rennt.arcticsunrise.data.api;


import android.net.Uri;

import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;

import rx.Observable;

public interface PubcrawlService {
    public static String CATALOG_CACHE_FLAG = "catalogCacheFlag";


    Observable<Catalog> getCatalogObservable(Edition edition);
    Observable<Catalog> getCatalogObservable(Edition edition, boolean useCache);

    Uri getUriFromIssue(Edition edition, Issue issue, String filename);

    Observable<Issue> populateIssueWithSections(Edition edition, Issue issue);


    Observable<Section> populateSectionWithArticles(Edition edition, Issue issue, Section section);

}
