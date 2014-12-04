package com.example.rennt.arcticsunrise.ui;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.example.rennt.arcticsunrise.ArcticSunriseApp;
import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.api.GelcapService;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by rennt on 12/1/14.
 */
public class IssueViewPagerAdapter extends FragmentPagerAdapter{
    private Issue issue;

    public IssueViewPagerAdapter(FragmentManager fm, Issue issue){
        super(fm);
        this.issue = issue;
    }

    @Override
    public int getCount() {
        return issue.getSections().size();
    }

    @Override
    public CharSequence getPageTitle(int position){
        return issue.getSections().get(position).getTitle();
    }

    @Override
    public Fragment getItem(int position) {
        SectionFragment fragment = new SectionFragment();
        fragment.setSection(issue.getSections().get(position));
        return fragment;
    }

    /**
     * A fragment that represents a section within a gelcap Issue.
     */
    public static class SectionFragment extends ListFragment {
        private Section section;
        private ArrayAdapter<String> adapter;
        @Inject GelcapService gelcapService;
        private List<Article> articles;

        public void setSection(Section s){
            this.section = s;
        }
        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static SectionFragment newInstance(int sectionPos) {
            SectionFragment f = new SectionFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("sectionPos", sectionPos);
            f.setArguments(args);

            return f;
        }

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ArcticSunriseApp app = ArcticSunriseApp.get(getActivity());
            app.inject(this);
            gelcapService.getSectionContent(section, new Response.Listener<List<Article>>() {
                @Override
                public void onResponse(final List<Article> response) {
                    recieveSectionArticles(response);
                }
            }, null);
        }


        private void recieveSectionArticles(List<Article> articles){
            this.articles = articles;

            ArrayList<String> titles = new ArrayList<String>();
            for (Article article : articles){
                titles.add(article.getHeadline());
            }
            adapter.addAll(titles);
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            Timber.i("Saving instance state " + section.getName());
            outState.putParcelable("section", section);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.layout_fragment, container, false);
            Timber.i("On create view");
            if (adapter != null){
                adapter.clear();
                ArrayList<String> titles = new ArrayList<String>();
                for (Article article : articles){
                    titles.add(article.getHeadline());
                }
                adapter.addAll(titles);
                setListAdapter(adapter);
            }
            return v;
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Timber.i("On activity created");
            if (adapter == null){
                adapter = new ArrayAdapter<String>(
                        getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());
                setListAdapter(adapter);
            }
        }
    }
}
