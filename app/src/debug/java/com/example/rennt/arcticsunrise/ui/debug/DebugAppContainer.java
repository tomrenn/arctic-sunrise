package com.example.rennt.arcticsunrise.ui.debug;

import android.app.Activity;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.rennt.arcticsunrise.AppContainer;
import com.example.rennt.arcticsunrise.BuildConfig;
import com.example.rennt.arcticsunrise.MockApiModule;
import com.example.rennt.arcticsunrise.R;
import com.google.common.base.Strings;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

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
    private static final DateFormat DATE_DISPLAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    // injected preferences
    private Activity activity;

    @InjectView(R.id.debug_content) ViewGroup content;

    @InjectView(R.id.debug_network_endpoint) Spinner endpointSpinner;

    @InjectView(R.id.debug_device_make) TextView deviceMakeView;
    @InjectView(R.id.debug_device_model) TextView deviceModelView;
    @InjectView(R.id.debug_device_resolution) TextView deviceResolutionView;
    @InjectView(R.id.debug_device_density) TextView deviceDensityView;
    @InjectView(R.id.debug_device_release) TextView deviceReleaseView;
    @InjectView(R.id.debug_device_api) TextView deviceApiView;

    @InjectView(R.id.debug_build_name) TextView buildNameView;
    @InjectView(R.id.debug_build_code) TextView buildCodeView;
    @InjectView(R.id.debug_build_sha) TextView buildShaView;
    @InjectView(R.id.debug_build_date) TextView buildDateView;



    @Inject public DebugAppContainer(){
        // init the container`
    }

    public ViewGroup get(Activity activity){
        this.activity = activity;
        activity.setContentView(R.layout.debug_activity_frame);

        ViewGroup drawer = findById(activity, R.id.debug_drawer);
        LayoutInflater.from(activity).inflate(R.layout.debug_drawer_content, drawer);

        ButterKnife.inject(this, activity);

        setupBuildSection();
        setupDeviceSection();
        setupEndpointConfigs();

        return content;
    }

    private void setupEndpointConfigs(){
        EnumAdapter<MockApiModule.Endpoint> endpointAdapter = new EnumAdapter<>(activity, MockApiModule.Endpoint.class);
        endpointSpinner.setAdapter(endpointAdapter);
    }

    private void setupBuildSection() {
        buildNameView.setText(BuildConfig.VERSION_NAME);
        buildCodeView.setText(String.valueOf(BuildConfig.VERSION_CODE));
        buildShaView.setText(BuildConfig.GIT_SHA);

        try {
            // Parse ISO8601-format time into local time.
            java.text.DateFormat inFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
            inFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date buildTime = inFormat.parse(BuildConfig.BUILD_TIME);
            buildDateView.setText(DATE_DISPLAY_FORMAT.format(buildTime));
        } catch (ParseException e) {
            throw new RuntimeException("Unable to decode build time: " + BuildConfig.BUILD_TIME, e);
        }
    }

    private static String truncateAt(String string, int length) {
        return string.length() > length ? string.substring(0, length) : string;
    }

    private void setupDeviceSection() {
        DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
        String densityBucket = getDensityString(displayMetrics);
        deviceMakeView.setText((truncateAt(Build.MANUFACTURER, 20)));
        deviceModelView.setText(truncateAt(Build.MODEL, 20));
        deviceResolutionView.setText(displayMetrics.heightPixels + "x" + displayMetrics.widthPixels);
        deviceDensityView.setText(displayMetrics.densityDpi + "dpi (" + densityBucket + ")");
        deviceReleaseView.setText(Build.VERSION.RELEASE);
        deviceApiView.setText(String.valueOf(Build.VERSION.SDK_INT));
    }

    private static String getDensityString(DisplayMetrics displayMetrics) {
        switch (displayMetrics.densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                return "ldpi";
            case DisplayMetrics.DENSITY_MEDIUM:
                return "mdpi";
            case DisplayMetrics.DENSITY_HIGH:
                return "hdpi";
            case DisplayMetrics.DENSITY_XHIGH:
                return "xhdpi";
            case DisplayMetrics.DENSITY_XXHIGH:
                return "xxhdpi";
            case DisplayMetrics.DENSITY_XXXHIGH:
                return "xxxhdpi";
            case DisplayMetrics.DENSITY_TV:
                return "tvdpi";
            default:
                return "unknown";
        }
    }
}
