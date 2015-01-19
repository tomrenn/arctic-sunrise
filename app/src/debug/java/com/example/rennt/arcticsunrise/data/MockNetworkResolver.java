package com.example.rennt.arcticsunrise.data;

import android.app.Application;
import android.content.res.AssetManager;
import android.net.Uri;

import com.squareup.okhttp.OkHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;

import timber.log.Timber;


public class MockNetworkResolver extends DefaultNetworkResolver {
    private AssetManager assets;

    public MockNetworkResolver(OkHttpClient httpClient, AssetManager assets) {
        super(httpClient);
        this.assets = assets;
    }

    @Override
    public String fetchUriToString(Uri uri) throws IOException {
        Timber.d("In Mock URI reader: " + uri);
        if ("mock".equals(uri.getScheme())){
            return convertStreamToString(assets.open(uri.getLastPathSegment()));
        } else {
            return super.fetchUriToString(uri);
        }
    }

    @Override
    public Reader fetchUri(Uri uri) throws IOException {
        Timber.d("In Mock URI reader: " + uri);
        if ("mock".equals(uri.getScheme())){
            return new BufferedReader(new InputStreamReader(assets.open(uri.getLastPathSegment())));
        } else {
            return super.fetchUri(uri);
        }
    }

    private String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
}

