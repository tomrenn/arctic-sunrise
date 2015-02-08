package com.example.rennt.arcticsunrise.data.api;

import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;

import rx.Observable;

/**
 * Created by tomrenn on 2/8/15.
 */
public interface IssueService {

    /**
     * Populate an Issue with it's sections. Issue Service here implicity has a
     * reference to the Issue, maybe all this should go into a single service...
     * FIXME: issue service problem described above..
     * @return
     */
    Observable<Issue> getIssueSectionsObservable();

    Observable<Section> buildSectionArticlesObservable(final int sectionPos);

    // Observable<FullArticle> populateArticle(); ??

    // Observable
}
