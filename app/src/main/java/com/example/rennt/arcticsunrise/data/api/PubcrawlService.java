package com.example.rennt.arcticsunrise.data.api;


import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;

import rx.Observable;

public interface PubcrawlService {

    Observable<Catalog> getCatalogObservable(Edition edition);
    Observable<Catalog> getCatalogObservable(Edition edition, boolean useCache);


    Observable<Issue> populateIssueWithSections(Edition edition, Issue issue);


    Observable<Section> populateSectionWithArticles(Edition edition, Issue issue, Section section);

}
