package com.example.rennt.arcticsunrise.data;

import android.app.Application;
import android.graphics.Bitmap;
import android.util.LruCache;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.example.rennt.arcticsunrise.data.api.GelcapService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by rennt on 11/16/14.
 */
@Module(
        includes = GelcapService.class,
        complete = false,
        library = true
)
public class DataModule {
    private static final int MAX_CACHE_SIZE = 20; // number of bitmaps in cache

    @Provides @Singleton RequestQueue providesRequestQueue(Application appContext) {
        return Volley.newRequestQueue(appContext.getApplicationContext());
    }

    @Provides @Singleton ImageLoader provideImageLoader(RequestQueue mRequestQueue) {
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
