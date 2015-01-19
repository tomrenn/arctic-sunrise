package com.example.rennt.arcticsunrise.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.rennt.arcticsunrise.data.api.BaseApiPath;
import com.example.rennt.arcticsunrise.data.api.BaseEditionPath;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.prefs.IssuePreference;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.example.rennt.arcticsunrise.data.prefs.StringPreference;
import com.example.rennt.arcticsunrise.ui.MainActivity;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Reader;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rennt on 11/16/14.
 */
@Module(
        complete = false, // because 'Application' is provided from another module
        library = true // because these providers are used outside of this module
)
public class DataModule {
    private static final int MAX_CACHE_SIZE = 20; // number of bitmaps in cache
    public static final String DEFAULT_API = "http://gelcap.dowjones.com/gc/packager/wsj";

    public interface NetworkResolver{
        String fetchUriToString(Uri uri) throws IOException;
        Reader fetchUri(Uri uri) throws IOException;
    }


    @Provides @Singleton
    OkHttpClient provideHttpClient() {
        return new OkHttpClient();
    }


    @Provides @BaseApiPath Uri provideBaseApiPath(){
        return Uri.parse(DEFAULT_API);
    }


    @Provides @Singleton Edition provideEdition() {
        return Edition.USA;
    }

    @Provides @Singleton
    SharedPreferences provideSharedPreferences(Application app){
        return app.getSharedPreferences("default", Application.MODE_PRIVATE);
    }

    @Provides @ApiEndpoint StringPreference provideApiEndpoint(SharedPreferences prefs){
        return new StringPreference(prefs, "ApiEndpoint", DEFAULT_API);
    }

    @Provides @IssuePreference LongPreference provideCurrentIssuePreference(SharedPreferences prefs){
        return new LongPreference(prefs, "CURRENT_ISSUE_ID");
    }

    @Provides @Singleton
    RequestQueue provideRequestQueue(Application appContext) {
        return Volley.newRequestQueue(appContext.getApplicationContext());
    }

    @Provides @Singleton
    Gson provideGson() {
        return new GsonBuilder().create();
    }

    @Provides @Singleton
    ImageLoader provideImageLoader(RequestQueue mRequestQueue) {
        // create new image loader, example from -
        // http://developer.android.com/training/volley/requestqueue.html#singleton
        return new ImageLoader(mRequestQueue, new ImageLoader.ImageCache() {
            private final LruCache<String, Bitmap>
                    cache = new LruCache<String, Bitmap>(MAX_CACHE_SIZE);

            @Override
            public Bitmap getBitmap(String url) {
                return cache.get(url);
            }

            @Override
            public void putBitmap(String url, Bitmap bitmap) {
                cache.put(url, bitmap);
            }
        });
    }

}
