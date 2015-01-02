package com.example.rennt.arcticsunrise.ui;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.astuetz.PagerSlidingTabStrip;
import com.example.rennt.arcticsunrise.AppContainer;
import com.example.rennt.arcticsunrise.ArcticSunriseApp;
import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.EditionModule;
import com.example.rennt.arcticsunrise.data.IssueModule;
import com.example.rennt.arcticsunrise.data.ObjectGraphHolder;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.IssueService;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;

//import org.lucasr.twowayview.TwoWayLayoutManager;
//import org.lucasr.twowayview.widget.SpannableGridLayoutManager;
//import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import dagger.ObjectGraph;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;
//import retrofit.converter.SimpleXMLConverter;


public class MainActivity extends ActionBarActivity implements Response.ErrorListener, ObjectGraphHolder {
    @Inject AppContainer appContainer;
    @Inject Observable<Catalog> catalogObservable;
    private ViewGroup container;
    @InjectView(R.id.drawerListView) ListView drawerListView;
    @InjectView(R.id.viewpager) ViewPager viewPager;
    @InjectView(R.id.pagertabs) PagerSlidingTabStrip pagerTabs;
    @InjectView(R.id.toolbar) Toolbar toolbar;

    // happens later
    private ObjectGraph issueObjectGraph;
    private IssueService issueService;

    long startTime = 0;
    // observables
    Subscription lastCatalogSubscription;

    @Override @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // do dep. injection.
        ArcticSunriseApp app = ArcticSunriseApp.get(this);
        ObjectGraph editionGraph = app.plusEditionModule(new EditionModule(Edition.USA));
        editionGraph.inject(this);

        // get root view
        container = appContainer.get(this);
        // set content view
        getLayoutInflater().inflate(R.layout.activity_main, container);

        // inject views
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        startTime = System.currentTimeMillis();
        subscribeNewCatalogRequest();
    }

    public ObjectGraph getObjectGraph(){
        return issueObjectGraph;
    }

    private void subscribeNewCatalogRequest() {
        lastCatalogSubscription = catalogObservable.subscribe(new Action1<Catalog>() {
            @Override
            public void call(Catalog catalog) {
                receiveCatalog(catalog);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void receiveCatalog(Catalog catalog){
        // request NOW issue
        Timber.d("Recieved Catalog object " + catalog);

        List<String> issues = new LinkedList<>();
        for (Issue issue : catalog.getIssues()){
            issues.add(issue.getKey());
        }
        drawerListView.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, issues));

        ArcticSunriseApp app = ArcticSunriseApp.get(this);
        issueObjectGraph = app.plusIssueModule(new IssueModule(catalog.getIssues().get(0)));
        issueService = issueObjectGraph.get(IssueService.class);

        // fill issue sections.
        Subscription subscription = issueService.getIssueSectionsObservable()
                .subscribe(new Action1<Issue>() {
                    @Override
                    public void call(Issue issue) {
                        receiveFilledIssue(issue);
                    }
                });
//        issueService.getIssue(catalog.getIssues().get(0), issueReciever, this);
    }

    private void receiveFilledIssue(final Issue issue){
        // do something

        // create PagedIssueAdapter
//        issueService.getSectionContent(issue.getSections().get(0), spr, this);

        Timber.d("Recived Issue on thread id " + android.os.Process.getThreadPriority(android.os.Process.myTid()));

        // notify adapter changed on main UI thread.
        Timber.d("updating view pager on thread id " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
        IssueViewPagerAdapter adapter = new IssueViewPagerAdapter(getSupportFragmentManager(), issue);
        viewPager.setAdapter(adapter);
        pagerTabs.setViewPager(viewPager);

        long totalTime = System.currentTimeMillis() - startTime;
        Toast.makeText(this, "Fetching catalog and sections took: " + totalTime, Toast.LENGTH_LONG).show();
//        pagerTabs.notifyDataSetChanged();
//                viewPager.invalidate();

//        viewPager.invalidate();
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        Timber.e(error.getMessage());
    }

}
