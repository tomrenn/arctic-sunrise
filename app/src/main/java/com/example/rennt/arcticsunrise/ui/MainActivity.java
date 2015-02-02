package com.example.rennt.arcticsunrise.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.example.rennt.arcticsunrise.data.api.CatalogService;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.IssueService;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.prefs.IssuePreference;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.melnykov.fab.FloatingActionButton;

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
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;
//import retrofit.converter.SimpleXMLConverter;


public class MainActivity extends ActionBarActivity implements ObjectGraphHolder {
    @Inject AppContainer appContainer;
    @Inject CatalogService catalogService;
    @Inject UserManager userManager;
    private ViewGroup container;

    @InjectView(R.id.navDrawer) DrawerLayout navDrawer;
    @InjectView(R.id.drawerListView) ListView drawerListView;
    @InjectView(R.id.viewpager) ViewPager viewPager;
    @InjectView(R.id.pagertabs) PagerSlidingTabStrip pagerTabs;
    @InjectView(R.id.toolbar) Toolbar toolbar;
    @InjectView(R.id.content) RelativeLayout relativeContent;
    @InjectView(R.id.progressBar) ProgressBar progressBar;

    @Inject @IssuePreference LongPreference savedIssuePref;

    // happens later
    private Catalog catalog;
    private ObjectGraph issueObjectGraph;
    private IssueService issueService;
    private Issue currentIssue;

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

        if (savedIssuePref.isSet()) {
            Timber.d("Saved Issue id is : " + savedIssuePref.get());
            container.removeView(progressBar);
            progressBar.setVisibility(View.GONE);
            restoreSavedIssue(savedIssuePref.get());
        }

        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.app_name);

        boolean useCatalogCache = true;
        Bundle args = getIntent().getExtras();
        if (args != null){
            useCatalogCache = args.getBoolean(CatalogService.CATALOG_CACHE_FLAG, true);
        }
        subscribeNewCatalogRequest(useCatalogCache);
    }


    private void restoreSavedIssue(final long issueId){
        Observable<Issue> savedIssueObsverable = Observable.create(new Observable.OnSubscribe<Issue>(){
            @Override
            public void call(final Subscriber<? super Issue> subscriber) {
                Issue issue = Issue.findById(Issue.class, issueId);
                subscriber.onNext(issue);
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());

        savedIssueObsverable.subscribe(new Action1<Issue>() {
            @Override
            public void call(Issue issue) {
                chooseIssue(issue);
            }
        });
    }


    protected void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        Timber.d("--- saving instance state ---");
        Timber.d("current issue : " + issueService.getIssue().getId());
        savedIssuePref.set(issueService.getIssue().getId());
        outState.putLong("savedIssueId", issueService.getIssue().getId());
    }

    public ObjectGraph getObjectGraph(){
        return issueObjectGraph;
    }

    private void subscribeNewCatalogRequest(boolean useCache) {
        Observable<Catalog> catalogObservable = catalogService.getCatalogObservable(useCache);

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

    private void setupNavDrawer(){
        List<String> issues = new LinkedList<>();
        for (Issue issue : catalog.getIssues()){
            issues.add(issue.getKey());
        }
        drawerListView.setAdapter(
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, issues));

        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Issue selectedIssue = catalog.getIssues().get(position);
                Timber.d("Selected position " + position);
                Timber.d("Changing to issue " + selectedIssue.getKey());
                chooseIssue(selectedIssue);
                navDrawer.closeDrawers();
            }
        });
    }

    private void receiveCatalog(Catalog catalog){
        Timber.d("Recieved Catalog object " + catalog);
        this.catalog = catalog;

        setupNavDrawer();

        if (currentIssue == null){
            Timber.d("Defaulting to first available issue");
            chooseIssue(catalog.getIssues().get(0));
        } else {
            checkOutdatedIssue(catalog);
        }
    }

    /**
     * Check if currentIssue is outdated.
     *
     * @return Null if not outdated, or the updated Issue.
     */
    private void checkOutdatedIssue(Catalog catalog){
        for (Issue issue : catalog.getIssues()){
            if (currentIssue.getKey().equals(issue.getKey())){
                alertNewAvailableIssue(issue);
                break;
//                if (issue.getRevision() > currentIssue.getRevision()){
//                    // show new content available dialog
//                    alertNewAvailableIssue(issue);
//                    break;
//                }
            }
        }
    }

    private void alertNewAvailableIssue(Issue issue){
        Toast.makeText(this, "New content available, pull to refresh", Toast.LENGTH_SHORT).show();
    }

    private void chooseIssue(Issue issue){
        container.removeView(progressBar);
        Timber.d("Chosen issue : " + issue.getKey() + " - " + issue.getId());
        currentIssue = issue;
        ArcticSunriseApp app = ArcticSunriseApp.get(this);
        issueObjectGraph = app.plusIssueModule(new IssueModule(issue));
        issueService = issueObjectGraph.get(IssueService.class);

        // fill issue sections.
        Subscription subscription = issueService.getIssueSectionsObservable()
                .subscribe(new Action1<Issue>() {
                    @Override
                    public void call(Issue issue) {
                        receiveFilledIssue(issue);
                    }
                });
    }

    private void receiveFilledIssue(final Issue issue){
        IssueViewPagerAdapter adapter = new IssueViewPagerAdapter(getFragmentManager(), this, issue);
        viewPager.setAdapter(adapter);
        pagerTabs.setViewPager(viewPager);

        pagerTabs.notifyDataSetChanged();
    }

}
