package com.example.rennt.arcticsunrise.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.net.Uri;

import com.example.rennt.arcticsunrise.data.api.BaseApiPath;
import com.example.rennt.arcticsunrise.data.api.SavedUserId;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.prefs.BooleanPreference;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.example.rennt.arcticsunrise.data.prefs.StringPreference;
import com.example.rennt.arcticsunrise.ui.debug.DebugAppContainer;
import com.squareup.okhttp.OkHttpClient;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(
        overrides = true,
        complete = false,
        library = true
)
public class DebugDataModule {


    @Provides @BaseApiPath Uri provideBaseApiPath(@ApiEndpoint StringPreference apiEndpoint){
        return Uri.parse(apiEndpoint.get());
    }

    @Provides @MockUserFlag BooleanPreference provideMockUser(SharedPreferences prefs){
        return new BooleanPreference(prefs, "MockUserFlag");
    }

    @Provides @ApiEndpoint StringPreference provideApiEndpoint(SharedPreferences prefs){
        return new StringPreference(prefs, "ApiEndpoint", DataModule.PRODUCTION_API_URL);
    }


    @Provides @Singleton
    UserManager provideUserManager(@MockUserFlag BooleanPreference mockUserFlag,
                                   @SavedUserId LongPreference savedUserId,
                                   OkHttpClient httpClient){
        return new MockUserManager(savedUserId, httpClient, mockUserFlag);
    }
    /**
     * A NetworkResolver to handle mock uri requests.
     */
    @Provides
    DataModule.NetworkResolver provideNetworkResolver(OkHttpClient httpClient, Application app){
        return new MockNetworkResolver(httpClient, app.getAssets());
    }


}
