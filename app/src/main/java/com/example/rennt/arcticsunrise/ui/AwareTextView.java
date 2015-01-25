package com.example.rennt.arcticsunrise.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import timber.log.Timber;

/**
 * Created by tomrenn on 1/24/15.
 */
public class AwareTextView extends TextView {
    private boolean measured = false;


    public AwareTextView(Context context) {
        super(context);
        init();
    }

    public AwareTextView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        init();
    }

    public AwareTextView(Context context, AttributeSet attributeSet, int defStyle){
        super(context, attributeSet, defStyle);
        init();
    }

    private void init(){
        this.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setMaxLines(Integer.MAX_VALUE);
                measured = false;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Timber.d("text: " + AwareTextView.this.getText());
                Timber.d("line height: " + getLineHeight());
                Timber.d("Pre draw for textview, height is :" + AwareTextView.this.getHeight());
                if (!measured){
                    int maxLines = (int)Math.floor(AwareTextView.this.getHeight() / getLineHeight());
                    setMaxLines(maxLines);
                    measured = true;
                }
                return true;
            }
        });
    }
}
