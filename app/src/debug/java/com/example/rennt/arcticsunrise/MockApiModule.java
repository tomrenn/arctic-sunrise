package com.example.rennt.arcticsunrise;

import android.app.Application;
import android.net.Uri;

import com.example.rennt.arcticsunrise.data.DataModule;
import com.example.rennt.arcticsunrise.data.api.BaseApiPath;

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


}
