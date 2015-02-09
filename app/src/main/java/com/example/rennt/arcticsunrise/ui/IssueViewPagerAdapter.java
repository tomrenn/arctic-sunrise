package com.example.rennt.arcticsunrise.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rennt.arcticsunrise.ArcticSunriseApp;
import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.PubcrawlIssueService;
import com.example.rennt.arcticsunrise.data.api.PubcrawlService;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.api.models.Article;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.Section;
import com.example.rennt.arcticsunrise.data.api.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;

/**
 * Section front PagerAdapter.
 */
public class IssueViewPagerAdapter extends FragmentStatePagerAdapter {
    private Issue issue;

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
        Fragment fragment = new SectionRecyclerFragment();

        Bundle args = new Bundle();
        args.putInt("sectionPos", position);
        fragment.setArguments(args);

        return fragment;
    }


    /**
     * Simple ViewHolder for article cards.
     */
    public static class CardViewHolder extends RecyclerView.ViewHolder {
        public TextView headline;
        public TextView key;
        public TextView summary;
        public ImageView image;

        public CardViewHolder(View itemView) {
            super(itemView);
            headline = findById(itemView, R.id.headline);
            summary = findById(itemView, R.id.summary);
            image = findById(itemView, R.id.image);
            key = findById(itemView, R.id.key);
        }
    }


    /**
     * Section Fragment using RecyclerView
     */
    public static class SectionRecyclerFragment extends Fragment implements
            UserManager.LoggedInListener, UserManager.LoggedOutListener {
        @Inject PubcrawlService pubcrawl;
        @Inject Edition edition;
        @Inject Issue issue;
        @Inject UserManager userManager;

        private int sectionPos;
        private RecyclerView recyclerView;
        private RecyclerView.Adapter recyclerAdapter;

        private Observable<Section> articleObserver;


        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            ArcticSunriseApp app = ArcticSunriseApp.get(getActivity());
            app.inject(this);

            sectionPos = getArguments().getInt("sectionPos");

            Section section = issue.getSections().get(sectionPos);
            this.articleObserver = pubcrawl.populateSectionWithArticles(edition, issue, section);
            articleObserver.subscribe(new Action1<Section>() {
                @Override
                public void call(Section section) {
                    recieveSectionArticles(section.getArticles());
                }
            });

            userManager.addListeners(this, this);
        }

        @Override
        public void onDestroyView() {
            super.onDestroyView();
            userManager.removeLoginListener(this);
            userManager.removeLogoutListener(this);
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
                private static final int DECO_CARD_TYPE = 2;

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
                        case DECO_CARD_TYPE:
                            view = inflater.inflate(R.layout.deco_card, parent, false);
                            break;
                        default:
                            throw new UnsupportedOperationException("Unknown card type");
                    }
                    return new CardViewHolder(view);
                }

                /**
                 * Determine what card to show based off Article properties
                 * @return The internal adapter type
                 */
                public int getItemViewType(int position){
                    if (articles.get(position).getThumbnail().isEmpty()) {
                        return CARD_TYPE;
                    } else if (articles.get(position).isDeco()) {
                        return DECO_CARD_TYPE;
                    } else {
                        return IMAGE_CARD_TYPE;
                    }
                }

                public boolean shouldShowKey(Article article, UserManager userManager){
                    return article.isPaid() &&
                           (userManager.getUser() == null ||
                                    !userManager.getUser().isPaidSubscriber());
                }

                /**
                 * Assign(bind) values to the ViewHolder
                 */
                @Override
                public void onBindViewHolder(CardViewHolder holder, int position) {
                    Article article = articles.get(position);
                    if (holder.key != null && shouldShowKey(article, userManager)) {
                        holder.key.setText(new String(new int[] { 0x1F511 }, 0, 1));
                    } else if (holder.key != null){
                        holder.key.setText("");
                    }
                    holder.headline.setText(article.getHeadline());
                    if (holder.summary != null){
                        holder.summary.setText(article.getSummary());
                    }
                    if (!article.getThumbnail().isEmpty()) {
                        Uri uri = pubcrawl.getUriFromIssue(edition, issue, article.getThumbnail());
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

        @Override
        public void onUserLoggedIn(User user) {
            recyclerView.getAdapter().notifyDataSetChanged();
            recyclerView.invalidate();
        }

        @Override
        public void onUserLoggedOut() {
            recyclerAdapter.notifyDataSetChanged();
            recyclerView.invalidate();
        }
    }



}
