package com.example.rennt.arcticsunrise.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.astuetz.PagerSlidingTabStrip;
import com.example.rennt.arcticsunrise.AppContainer;
import com.example.rennt.arcticsunrise.ArcticSunriseApp;
import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.api.GelcapService;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.data.api.models.SectionPage;

//import org.lucasr.twowayview.TwoWayLayoutManager;
//import org.lucasr.twowayview.widget.SpannableGridLayoutManager;
//import org.lucasr.twowayview.widget.TwoWayView;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;
//import retrofit.converter.SimpleXMLConverter;


public class MainActivity extends ActionBarActivity implements Response.ErrorListener {
    @Inject GelcapService gelcapService;
    @Inject AppContainer appContainer;

    private ViewGroup container;
    @InjectView(R.id.viewpager) ViewPager viewPager;
    @InjectView(R.id.pagertabs) PagerSlidingTabStrip pagerTabs;
    @InjectView(R.id.toolbar) Toolbar toolbar;

    long startTime = 0;
    // observables
    Observable<Catalog> catalogObservable;
    Subscription lastCatalogSubscription;

    @Override @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // do dep. injection.
        ArcticSunriseApp app = ArcticSunriseApp.get(this);
        app.inject(this);

        // get root view
        container = appContainer.get(this);
        // set content view
        getLayoutInflater().inflate(R.layout.activity_main, container);

        // inject views
        ButterKnife.inject(this);

        setSupportActionBar(toolbar);

        startTime = System.currentTimeMillis();
        catalogObservable = gelcapService.getCatalogObservable();
        subscribeNewCatalogRequest();
    }

    private void subscribeNewCatalogRequest() {
        lastCatalogSubscription = catalogObservable.subscribe(new Action1<Catalog>() {
            @Override
            public void call(Catalog catalog) {
                receiveCatalog(catalog);
            }
        });
    }
//
//    private void setupTwoWayView(){
//        TwoWayView twowayView = (TwoWayView) findViewById(R.id.cardList);
//
//        SpannableGridLayoutManager spannableGrid = new SpannableGridLayoutManager(
//                TwoWayLayoutManager.Orientation.VERTICAL,
//                5, // columns
//                10 // rows
//        );
//        twowayView.setLayoutManager(spannableGrid);
//
////        twowayView.setAdapter(new MyAdapter(true));
//    }


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

        // fill issue sections.
        Subscription subscription = gelcapService.getIssueSectionsObservable(
                catalog.getIssues().get(0)).subscribe(new Action1<Issue>() {
            @Override
            public void call(Issue issue) {
                receiveFilledIssue(issue);
            }
        });
//        gelcapService.getIssue(catalog.getIssues().get(0), issueReciever, this);
    }

    private void receiveFilledIssue(final Issue issue){
        // do something

        // create PagedIssueAdapter
//        gelcapService.getSectionContent(issue.getSections().get(0), spr, this);

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
