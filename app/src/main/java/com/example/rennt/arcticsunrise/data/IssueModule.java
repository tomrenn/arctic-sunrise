package com.example.rennt.arcticsunrise.data;

import android.net.Uri;

import com.example.rennt.arcticsunrise.ArcticSunriseModule;
import com.example.rennt.arcticsunrise.data.api.BaseEditionPath;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.IssueService;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.ui.IssueViewPagerAdapter;
import com.example.rennt.arcticsunrise.ui.MainActivity;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.squareup.okhttp.OkHttpClient;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import dagger.Module;
import dagger.Provides;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;


@Module(
        injects = {
                MainActivity.class,
                IssueService.class,
                IssueViewPagerAdapter.SectionRecyclerFragment.class,
                IssueViewPagerAdapter.SectionFragment.class
        },
        addsTo = ArcticSunriseModule.class,
        complete = false, library = true
)
public class IssueModule {
    private Issue issue;


    public IssueModule(Issue issue){
        this.issue = issue;
    }


    @Provides
    IssueService provideIssueService(DataModule.NetworkResolver resolver, Gson gson, @BaseEditionPath Uri editionBase){
        return new IssueService(resolver, gson, issue, editionBase);
    }

}
