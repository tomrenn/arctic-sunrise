package com.example.rennt.arcticsunrise.ui;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.ImageView;

import com.example.rennt.arcticsunrise.R;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

/**
 * Created by tomrenn on 2/17/15.
 */
public class ArticlesActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.simple_article);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findViewById(R.id.imageView).setTransitionName("image");
        }
        Timber.i("image url: " + getIntent().getExtras().getString("imageUrl"));

        Picasso.with(this).load(getIntent().getExtras().getString("imageUrl")).into(
                (ImageView)findViewById(R.id.imageView));
    }
}
