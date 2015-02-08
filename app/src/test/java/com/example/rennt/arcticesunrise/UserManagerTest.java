package com.example.rennt.arcticesunrise;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.mock.MockApplication;
import android.test.mock.MockContext;

import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.example.rennt.arcticsunrise.ui.IssueViewPagerAdapter;
import com.squareup.okhttp.OkHttpClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.mockito.Mockito.*;


public class UserManagerTest {
    UserManager userManager;

    @Before
    public void setup(){
        SharedPreferences prefs = mock(SharedPreferences.class);
        this.userManager = new UserManager(new LongPreference(prefs, "test"), new OkHttpClient());
    }

    @Test
    public void testUserObservable(){
        assertTrue(false);
        IssueViewPagerAdapter.SectionRecyclerFragment frag = new IssueViewPagerAdapter.SectionRecyclerFragment();
    }
}