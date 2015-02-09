package com.example.rennt.arcticsunrise.data;

import android.net.Uri;

import com.example.rennt.arcticsunrise.ArcticSunriseModule;
import com.example.rennt.arcticsunrise.data.api.BaseEditionPath;
import com.example.rennt.arcticsunrise.data.api.IssueService;
import com.example.rennt.arcticsunrise.data.api.PubcrawlIssueService;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.ui.IssueViewPagerAdapter;
import com.example.rennt.arcticsunrise.ui.MainActivity;
import com.google.gson.Gson;

import dagger.Module;
import dagger.Provides;


@Module(
        injects = {
                MainActivity.class,
                PubcrawlIssueService.class,
                IssueViewPagerAdapter.SectionRecyclerFragment.class
        },
        addsTo = ArcticSunriseModule.class,
        complete = false, library = true
)
public class IssueModule {
    private Issue issue;


    public IssueModule(Issue issue){
        this.issue = issue;
    }

    @Provides Issue provideIssue(){ return issue; }

    @Provides
    IssueService provideIssueService(DataModule.NetworkResolver resolver, Gson gson, @BaseEditionPath Uri editionBase){
        return new PubcrawlIssueService(resolver, gson, issue, editionBase);
    }

}
