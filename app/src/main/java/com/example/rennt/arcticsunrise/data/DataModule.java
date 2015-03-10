package com.example.rennt.arcticsunrise.data;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.LruCache;

import com.example.rennt.arcticsunrise.data.api.BaseApiPath;
import com.example.rennt.arcticsunrise.data.api.BaseEditionPath;
import com.example.rennt.arcticsunrise.data.api.BasePubcrawlService;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.PubcrawlService;
import com.example.rennt.arcticsunrise.data.api.SavedUserId;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.prefs.BooleanPreference;
import com.example.rennt.arcticsunrise.data.prefs.IssuePreference;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.example.rennt.arcticsunrise.data.prefs.StringPreference;
import com.example.rennt.arcticsunrise.ui.IssueViewPagerAdapter;
import com.example.rennt.arcticsunrise.ui.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Reader;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import timber.log.Timber;

/**
 * Created by rennt on 11/16/14.
 */
@Module(
        injects = IssueViewPagerAdapter.class,
        complete = false, // because 'Application' is provided from another module
        library = true // because these providers are used outside of this module
)
public class DataModule {
    private static final int MAX_CACHE_SIZE = 20; // number of bitmaps in cache
    public static final String PRODUCTION_API_URL = "http://gelcap.dowjones.com/gc/packager/wsj";

    public interface NetworkResolver{
        String fetchUriToString(Uri uri) throws IOException;
        Reader fetchUri(Uri uri) throws IOException;
    }

    @Provides @Singleton
    PubcrawlService providePubcrawlService(@BaseApiPath Uri basePath, Gson gson,
                                           OkHttpClient httpClient, ConnectivityManager cm){
        return new BasePubcrawlService(basePath, cm, gson, httpClient);
    }

    @Provides @Singleton
    UserManager provideUserManager(@SavedUserId LongPreference savedUserId,
                                   OkHttpClient httpClient){
        return new UserManager(savedUserId, httpClient);
    }

    @Provides @Singleton
    OkHttpClient provideHttpClient() {
        return new OkHttpClient();
    }


    @Provides
    ConnectivityManager provideConnectivityManager(Application context){
        return ((ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE));
    }

    @Provides @BaseApiPath Uri provideBaseApiPath(){
        return Uri.parse(PRODUCTION_API_URL);
    }


    @Provides @Singleton Edition provideEdition() {
        return Edition.USA;
    }

    @Provides @Singleton
    SharedPreferences provideSharedPreferences(Application app){
        return app.getSharedPreferences("default", Application.MODE_PRIVATE);
    }

    @Provides @SavedUserId LongPreference provideSavedUserId(SharedPreferences prefs){
        return new LongPreference(prefs, "SavedUserId");
    }

    @Provides @IssuePreference LongPreference provideCurrentIssuePreference(SharedPreferences prefs){
        return new LongPreference(prefs, "CURRENT_ISSUE_ID");
    }

    @Provides @Singleton
    Gson provideGson() {
        return new GsonBuilder().create();
    }

}
