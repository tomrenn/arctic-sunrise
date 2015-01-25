package com.example.rennt.arcticsunrise.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.ListFragment;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.rennt.arcticsunrise.ArcticSunriseApp;
import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.api.IssueService;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.data.prefs.BooleanPreference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import rx.Observable;
import rx.functions.Action1;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;

/**
 * Created by rennt on 12/1/14.
 */
public class IssueViewPagerAdapter extends FragmentStatePagerAdapter {
    private Issue issue;
    @Inject @Named("UI-list") BooleanPreference useCardsFragment;

    public IssueViewPagerAdapter(FragmentManager fm, Context c, Issue issue){
        super(fm);
        this.issue = issue;
        ArcticSunriseApp.get(c).inject(this);
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
        Fragment fragment;
        if (useCardsFragment != null && useCardsFragment.get()) {
            fragment = new SectionRecyclerFragment();
        } else {
            fragment = new SectionFragment();
        }

        Bundle args = new Bundle();
        args.putInt("sectionPos", position);
        fragment.setArguments(args);

        return fragment;
    }


    /**
     * Section Fragment using RecyclerView
     */
    public static class SectionRecyclerFragment extends Fragment {
        @Inject IssueService issueService;
        private int sectionPos;
        private RecyclerView recyclerView;
        private RecyclerView.Adapter recyclerAdapter;

        private Observable<Section> articleObserver;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ArcticSunriseApp app = ArcticSunriseApp.get(getActivity());
            app.inject(this);

            sectionPos = getArguments().getInt("sectionPos");

            this.articleObserver = issueService.buildSectionArticlesObservable(sectionPos);
            articleObserver.subscribe(new Action1<Section>() {
                @Override
                public void call(Section section) {
                    recieveSectionArticles(section.getArticles());
                }
            });
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            recyclerView = (RecyclerView) inflater.inflate(R.layout.section, container, false);
            LinearLayoutManager manager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(manager);
            return recyclerView;
        }

        private void recieveSectionArticles(final List<Article> articles){
            recyclerAdapter = new RecyclerView.Adapter<CardViewHolder>() {
                private static final int CARD_TYPE = 0;
                private static final int IMAGE_CARD_TYPE = 1;

                @Override
                public CardViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                    View view;
                    LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                    Timber.d("On create view holder type: " + viewType);
                    switch (viewType){
                        case CARD_TYPE:
                            view = inflater.inflate(R.layout.simple_card, parent, false);
                            break;
                        case IMAGE_CARD_TYPE:
                            view = inflater.inflate(R.layout.right_image_card, parent, false);
                            break;
                        default:
                            view = null; // should NEVER happen
                    }
                    Timber.d("Created view - " + view);
                    return new CardViewHolder(view);
                }

                public int getItemViewType(int position){
                    if (articles.get(position).getThumbnail().isEmpty()){
                        return CARD_TYPE;
                    } else {
                        return IMAGE_CARD_TYPE;
                    }
                }

                /** Set view data */
                @Override
                public void onBindViewHolder(CardViewHolder holder, int position) {
                    Timber.d("Binding view holder - " + holder);
                    Article article = articles.get(position);
                    holder.headline.setText(article.getHeadline());
                    holder.summary.setText(article.getSummary());
                    if (!article.getThumbnail().isEmpty()) {
                        Uri uri = issueService.getUriFromIssue(article.getThumbnail());
                        Picasso.with(getActivity()).load(uri).into(holder.image);
                    }
                }

                @Override
                public int getItemCount() {
                    return articles.size();
                }
            };
           recyclerView.setAdapter(recyclerAdapter);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            Timber.i("Saving section fragment " + sectionPos);
            outState.putInt("sectionPos", sectionPos);
        }
    }

    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public TextView headline;
        public TextView summary;
        public ImageView image;

        public CardViewHolder(View itemView) {
            super(itemView);
            headline = findById(itemView, R.id.headline);
            summary = findById(itemView, R.id.summary);
            image = findById(itemView, R.id.image);
        }
    }

    /**
     * A fragment that represents a section within a gelcap Issue.
     */
    public static class SectionFragment extends ListFragment {
        @Inject IssueService issueService;
        private int sectionPos;
        private ArrayAdapter<String> adapter;
        private List<Article> articles;
        private long startTime = 0;
        private Observable<Section> articleObserver;
        //todo: inject add Floating Action Button if new content

        /**
         * When creating, retrieve this instance's number from its arguments.
         */
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ArcticSunriseApp app = ArcticSunriseApp.get(getActivity());
            app.inject(this);
            startTime = System.currentTimeMillis();

            sectionPos = getArguments().getInt("sectionPos");

            // setSection must be called before attempting to use this fragment.
            this.articleObserver = issueService.buildSectionArticlesObservable(sectionPos);
            articleObserver.subscribe(new Action1<Section>() {
                @Override
                public void call(Section section) {
                    recieveSectionArticles(section.getArticles());
                }
            });
        }


        private void recieveSectionArticles(List<Article> articles){
            long totalTime = System.currentTimeMillis() - startTime;
            Toast.makeText(getActivity(), "section " + sectionPos + " took " + totalTime + "ms", Toast.LENGTH_SHORT).show();

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
            Timber.i("Saving section fragment " + sectionPos);
            outState.putInt("sectionPos", sectionPos);
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.layout_fragment, container, false);
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
            if (adapter == null){
                adapter = new ArrayAdapter<String>(
                        getActivity(), android.R.layout.simple_list_item_1, new ArrayList<String>());
                setListAdapter(adapter);
            }
        }
    }
}
