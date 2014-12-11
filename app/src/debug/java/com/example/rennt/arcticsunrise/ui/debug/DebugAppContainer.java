package com.example.rennt.arcticsunrise.ui.debug;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.example.rennt.arcticsunrise.AppContainer;
import com.example.rennt.arcticsunrise.R;

import javax.inject.Inject;
import javax.inject.Singleton;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static butterknife.ButterKnife.findById;

/**
 * Created by rennt on 12/10/14.
 */
@Singleton
public class DebugAppContainer implements AppContainer {
    // injected preferences
    private Activity activity;

    @InjectView(R.id.debug_content) ViewGroup content;

    @Inject public DebugAppContainer(){
        // init the container`
    }

    public ViewGroup get(Activity activity){
        this.activity = activity;
        activity.setContentView(R.layout.debug_activity_frame);

        ViewGroup drawer = findById(activity, R.id.debug_drawer);
        LayoutInflater.from(activity).inflate(R.layout.debug_drawer_content, drawer);

        ButterKnife.inject(this, activity);

        return content;
    }
}
