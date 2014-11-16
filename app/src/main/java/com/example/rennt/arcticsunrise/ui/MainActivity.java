package com.example.rennt.arcticsunrise.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.rennt.arcticsunrise.GelcapService;
import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.api.models.Catalog;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.IssueWrapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Collection;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.converter.GsonConverter;
//import retrofit.converter.SimpleXMLConverter;


public class MainActivity extends Activity {
    private GelcapService gelcap;
    private GelcapService XmlGelcap;

    private static final String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final long start = System.currentTimeMillis();

        Type issueListType = new TypeToken<Collection<IssueWrapper>>() {}.getType();

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Issue.class, new Issue.IssueDeserializer())
                .create();

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setConverter(new GsonConverter(gson))
                .setEndpoint("http://gelcap.dowjones.com")
                .build();


        // get concrete issue and sections in that issue.
        final Callback issueCallback = new Callback<Issue>() {
            @Override
            public void success(Issue issue, Response response) {
                int a = 4;
                long duration = System.currentTimeMillis() - start;
                Log.d(TAG, "Time for concrete issue took " + duration);





            }

            @Override
            public void failure(RetrofitError error) {
                Log.e("MyActivity", error.toString());
            }
        };

        // get catalog and issue wrappers
        Callback callback = new Callback<Catalog>() {
            @Override
            public void success(Catalog catalog, Response response) {
                Log.d("MyActivity", "in success");
                // issueId for the now issue
                String issueId = catalog.getIssues().get(0).getIssueId();
                Log.d("MyActivity", issueId);
                gelcap.getIssue(issueId, issueCallback);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e("MyActivity", retrofitError.toString());
            }
        };


        gelcap = restAdapter.create(GelcapService.class);
        gelcap.getCatalog(callback);
        Log.d("MyActivity", "After issuing callback");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
