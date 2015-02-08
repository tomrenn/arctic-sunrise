package com.example.rennt.arcticsunrise.ui;

import android.content.res.Resources;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rennt.arcticsunrise.R;


/**
 * Created by rennt on 11/17/14.
 */
public class MyAdapter extends RecyclerView.Adapter<MyAdapter.CardViewHolder> {
    private String[] items = {
            "One", "Two", "Three", "Four", "Five"
    };
    private final boolean spannableLayout;


    public MyAdapter(boolean spannableLayout){
        this.spannableLayout = spannableLayout;
    }

    @Override
    public CardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.example_card, viewGroup, false);

        return new CardViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(CardViewHolder cardViewHolder, int pos) {
        String item = items[pos];

        View itemView = cardViewHolder.itemView;
        Resources r = itemView.getResources();
//        if (pos % 2 == 0){
//            CardView card = (CardView) itemView;
//            itemView.setBackgroundColor(r.getColor(R.color.));
//        } else {
//            itemView.setBackgroundColor(r.getColor(R.color.cardview_light_background));
//        }

        // change spannable based on item (or pos)
        if (spannableLayout && pos==0) {
            setSpan(5, 2, cardViewHolder.itemView);
        }
        else if (spannableLayout && pos==1) {
            setSpan(1, 3, cardViewHolder.itemView);
            ((ImageView) cardViewHolder.itemView.findViewById(R.id.cardImage)).setImageResource(R.drawable.ic_launcher);
        }
        else if (spannableLayout) {
            setSpan(2, 1, cardViewHolder.itemView);
        }

        cardViewHolder.label.setText(item);
    }

    private void setSpan(int colSpan, int rowSpan, View itemView){
//        SpannableGridLayoutManager.LayoutParams params = new SpannableGridLayoutManager.LayoutParams(itemView.getLayoutParams());
//        params.rowSpan = rowSpan;
//        params.colSpan = colSpan;
//        itemView.setLayoutParams(params);
    }

    @Override
    public int getItemCount() {
        return items.length;
    }

    public final static class CardViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        ImageView image;

        public CardViewHolder(View itemView) {
            super(itemView);
            label = (TextView) itemView.findViewById(R.id.cardLabel);
            image = (ImageView) itemView.findViewById(R.id.cardImage);
        }
    }


    // TODO: ideally this class would have the data model to know which items get spanned
    public static class DecoSpansLookup extends GridLayoutManager.SpanSizeLookup {
        public int getSpanSize(int pos){
            if (pos==0){
                return 2; // span 2 columns
            }
            else{
                return 1;
            }
        }
    }
}


