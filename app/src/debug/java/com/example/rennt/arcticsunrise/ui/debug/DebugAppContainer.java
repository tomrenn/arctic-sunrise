package com.example.rennt.arcticsunrise.ui.debug;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.example.rennt.arcticsunrise.AppContainer;
import com.example.rennt.arcticsunrise.ArcticSunriseApp;
import com.example.rennt.arcticsunrise.BuildConfig;
import com.example.rennt.arcticsunrise.MockApiModule;
import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.ApiEndpoint;
import com.example.rennt.arcticsunrise.data.ApiEndpoints;
import com.example.rennt.arcticsunrise.data.MockUserFlag;
import com.example.rennt.arcticsunrise.data.api.CatalogService;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.prefs.BooleanPreference;
import com.example.rennt.arcticsunrise.data.prefs.IssuePreference;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.example.rennt.arcticsunrise.data.prefs.StringPreference;
import com.example.rennt.arcticsunrise.ui.MainActivity;
import com.google.common.base.Strings;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;

/**
 * Created by rennt on 12/10/14.
 */
@Singleton
public class DebugAppContainer implements AppContainer {
    private static final DateFormat DATE_DISPLAY_FORMAT = new SimpleDateFormat("yyyy-MM-dd hh:mm a");
    private StringPreference apiEndpoint;
    private LongPreference savedIssue;
    private BooleanPreference listUiType;
    private BooleanPreference mockUserFlag;
    private Application app;
    private UserManager userManager;
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

    @InjectView(R.id.debug_mock_user) Switch switchMockUser;


    @Inject public DebugAppContainer(
            @ApiEndpoint StringPreference apiEndpoint,
            @IssuePreference LongPreference savedIssue,
            @MockUserFlag BooleanPreference mockUserFlag,
            UserManager userManager,
            Application app
    ){
        this.apiEndpoint = apiEndpoint;
        this.savedIssue = savedIssue;
        this.mockUserFlag = mockUserFlag;
        this.userManager = userManager;
        this.app = app;
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
        setupUIConfigs();

        return content;
    }

    private void setupEndpointConfigs(){
        final ApiEndpoints currentEndpoint = ApiEndpoints.from(apiEndpoint.get());
        final EnumAdapter<ApiEndpoints> endpointAdapter = new EnumAdapter<>(activity, ApiEndpoints.class);
        endpointSpinner.setAdapter(endpointAdapter);
        endpointSpinner.setSelection(currentEndpoint.ordinal());

        endpointSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                ApiEndpoints selected = endpointAdapter.getItem(position);
                if (selected != currentEndpoint) {
                    if (selected == ApiEndpoints.CUSTOM) {
                        Timber.d("Custom network endpoint selected. Prompting for URL.");
//                        showCustomEndpointDialog(currentEndpoint.ordinal(), "http://");
                    } else {
                        setEndpointAndRelaunch(selected.url);
                    }
                } else {
                    Timber.d("Ignoring re-selection of network endpoint %s", selected);
                }
            }

            @Override public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
    }

    private void setupUIConfigs(){
        switchMockUser.setChecked(mockUserFlag.get());
        switchMockUser.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mockUserFlag.set(isChecked);
                if (isChecked) {
                    userManager.retrieveSavedUser().subscribe();
                }
                else {
                    userManager.logout();
                }
            }
        });
    }

    private void relaunch(){
        Intent newApp = new Intent(app, MainActivity.class);
        newApp.putExtra(CatalogService.CATALOG_CACHE_FLAG, false);
        newApp.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        app.startActivity(newApp);
        ArcticSunriseApp.get(app).buildObjectGraph();
    }

    private void setEndpointAndRelaunch(String endpoint) {
        Timber.d("Setting network endpoint to %s", endpoint);
        apiEndpoint.set(endpoint);
        savedIssue.delete();

        relaunch();
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
