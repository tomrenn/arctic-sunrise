package com.example.rennt.arcticsunrise.data;

import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.io.Reader;

import javax.inject.Inject;

/**
 * Delegate network requests.
 */
public class DefaultNetworkResolver implements DataModule.NetworkResolver{
    private OkHttpClient httpClient;

    @Inject DefaultNetworkResolver(OkHttpClient httpClient){
        this.httpClient = httpClient;
    }

    private Response performRequest(Uri uri) throws IOException {
        Request request = new Request.Builder()
                .url(uri.toString())
                .build();
        return httpClient.newCall(request).execute();
    }

    @Override
    public String fetchUriToString(Uri uri) throws IOException{
        return performRequest(uri).body().string();
    }

    @Override
    public Reader fetchUri(Uri uri) throws IOException{
        return performRequest(uri).body().charStream();
    }
}
