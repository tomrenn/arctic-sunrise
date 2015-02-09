package com.example.rennt.arcticsunrise;

import android.app.Application;
import android.net.Uri;

import com.example.rennt.arcticsunrise.data.ApiEndpoint;
import com.example.rennt.arcticsunrise.data.ApiEndpoints;
import com.example.rennt.arcticsunrise.data.DataModule;
import com.example.rennt.arcticsunrise.data.IsMockMode;
import com.example.rennt.arcticsunrise.data.api.BaseApiPath;
import com.example.rennt.arcticsunrise.data.prefs.StringPreference;

import dagger.Module;
import dagger.Provides;

/**
 * Mock the EditionBasePath for debug builds
 */
@Module(
        overrides = true,
        library = true
)
public class MockApiModule {

    @IsMockMode boolean provideMockFlag(@ApiEndpoint StringPreference apiEndpoint){
        return apiEndpoint.get().startsWith("mock://");
    }
}
