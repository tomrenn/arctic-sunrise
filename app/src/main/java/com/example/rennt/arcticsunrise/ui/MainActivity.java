package com.example.rennt.arcticsunrise.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Adapter;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.astuetz.PagerSlidingTabStrip;
import com.example.rennt.arcticsunrise.ArcticSunriseApp;
import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.api.GelcapService;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.data.api.models.SectionPage;

import org.lucasr.twowayview.TwoWayLayoutManager;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import hugo.weaving.DebugLog;
import timber.log.Timber;
//import retrofit.converter.SimpleXMLConverter;


public class MainActivity extends FragmentActivity implements Response.ErrorListener {
    @Inject GelcapService gelcapService;
    private CatalogReciever catalogReciever = new CatalogReciever();
    private IssueReciever issueReciever = new IssueReciever();
    private SectionPageReciever spr = new SectionPageReciever();
    @InjectView(R.id.viewpager) ViewPager viewPager;
    @InjectView(R.id.pagertabs) PagerSlidingTabStrip pagerTabs;

    @Override @DebugLog
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // do dep. injection.
        ArcticSunriseApp app = ArcticSunriseApp.get(this);
        app.inject(this);
        // inject views
        ButterKnife.inject(this);

//        setupTwoWayView();
        Timber.d("This is an example: " + gelcapService.getRequestQueue());
        Timber.d("The id of the thing is: " + gelcapService);
        gelcapService.getCatalog(catalogReciever, this);
    }

    private void setupTwoWayView(){
        TwoWayView twowayView = (TwoWayView) findViewById(R.id.cardList);

        SpannableGridLayoutManager spannableGrid = new SpannableGridLayoutManager(
                TwoWayLayoutManager.Orientation.VERTICAL,
                5, // columns
                10 // rows
        );
        twowayView.setLayoutManager(spannableGrid);

//        twowayView.setAdapter(new MyAdapter(true));
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
        gelcapService.getIssue(catalog.getIssues().get(0), issueReciever, this);
    }

    private void receiveIssue(final Issue issue){
        // do something
        Timber.d("Recieved Issue object " + issue);
        for (Section section : issue.getSections()){
            Timber.d("Reading section from issue -> " + section.getTitle());
        }
        // create PagedIssueAdapter
        Timber.d("Done iterating sections");
//        gelcapService.getSectionContent(issue.getSections().get(0), spr, this);

        Timber.d("Recived Issue on thread id " + android.os.Process.getThreadPriority(android.os.Process.myTid()));

        // notify adapter changed on main UI thread.
        Timber.d("updating view pager on thread id " + android.os.Process.getThreadPriority(android.os.Process.myTid()));
        IssueViewPagerAdapter adapter = new IssueViewPagerAdapter(getSupportFragmentManager(), issue);
        viewPager.setAdapter(adapter);
        viewPager.setBackgroundColor(getResources().getColor(R.color.primary_material_light));
        pagerTabs.setViewPager(viewPager);
        pagerTabs.notifyDataSetChanged();
//                viewPager.invalidate();

//        viewPager.invalidate();
    }

    private void receiveSection(List<Article> articles){
        // TODO: How should we handle received sections? should be saved to section attribute.
        // TODO: Subclass Response objects so that they do cacheing?
        // TODO: LocalPersistance class that can also send Response<T> for given listeners

        // TODO: With RxJava we would have a catalog observer, issue observer,
        // TODO: and section filled observer
        TextView tv = (TextView)findViewById(R.id.my_text_view);
        String output = "";
        for (Article article : articles) {
            output += article.getHeadline() + '\n';
        }
        tv.setText(output);
    }

    @Override
    public void onErrorResponse(VolleyError error) {
        Timber.e(error.getMessage());
    }

    private class SectionPageReciever implements Response.Listener<List<Article>> {
        @Override
        public void onResponse(List<Article> sectionPage) {
            receiveSection(sectionPage);
        }
    }

    private class IssueReciever implements Response.Listener<Issue> {
        @Override
        public void onResponse(Issue issue) {
            receiveIssue(issue);
        }
    }

    private class CatalogReciever implements Response.Listener<Catalog> {
        @Override
        public void onResponse(Catalog catalog) {
            receiveCatalog(catalog);
        }
    }
}
