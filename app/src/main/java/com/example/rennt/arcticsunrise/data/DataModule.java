package com.example.rennt.arcticsunrise.data;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.rennt.arcticsunrise.data.api.Edition;
import com.example.rennt.arcticsunrise.data.api.GelcapService;
import com.squareup.okhttp.OkHttpClient;

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
//    private final Edition edition;
//
//    public DataModule(Edition edition) {
//        this.edition = edition;
//    }

    @Provides @Singleton
    OkHttpClient provideHttpClient() {
        return new OkHttpClient();
    }

    @Provides @Singleton Edition provideEdition() {
        return Edition.USA;
    }

    @Provides @Singleton
    SharedPreferences provideSharedPreferences(Application app){
        return app.getSharedPreferences("default", Application.MODE_PRIVATE);
    }

    @Provides @Singleton
    RequestQueue provideRequestQueue(Application appContext) {
        return Volley.newRequestQueue(appContext.getApplicationContext());
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
