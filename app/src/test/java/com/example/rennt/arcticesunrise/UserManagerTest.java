package com.example.rennt.arcticesunrise;


import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.test.mock.MockApplication;
import android.test.mock.MockContext;

import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.api.models.User;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.example.rennt.arcticsunrise.ui.IssueViewPagerAdapter;
import com.squareup.okhttp.OkHttpClient;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.lang.reflect.Field;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;


public class UserManagerTest {
    UserManager userManager;
    Long fakeUserId = 3l;
    User fakeUser = new User("TestUser", "TestUser", "example@test.com", true);

    @Before
    public void setup(){
        LongPreference userPref = mock(LongPreference.class);
        this.userManager = new UserManager(userPref, new OkHttpClient());
    }

    private void setTestUser() {
        try {
            Field userField = UserManager.class.getDeclaredField("user");
            userField.setAccessible(true);
            userField.set(userManager, fakeUser);
        } catch (IllegalAccessException|NoSuchFieldException exception) {
            fail(exception.toString());
        }
    }

    /**
     * Tests .getUser() and .isUserLoaded() based on private 'user' state.
     */
    @Test
    public void testUserAvailability(){
        assertNull(userManager.getUser());
        assertFalse(userManager.isUserLoaded());

        setTestUser();
        assertEquals(fakeUser, userManager.getUser());
        assertTrue(userManager.isUserLoaded());

        userManager.logout();
        assertNull(userManager.getUser());
        assertFalse(userManager.isUserLoaded());
    }
}