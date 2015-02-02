package com.example.rennt.arcticsunrise.data;

import com.example.rennt.arcticsunrise.data.api.SavedUserId;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.api.models.User;
import com.example.rennt.arcticsunrise.data.prefs.BooleanPreference;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.squareup.okhttp.OkHttpClient;

import rx.Observable;


public class MockUserManager extends UserManager {
    private BooleanPreference mockUserFlag;

    public MockUserManager(LongPreference savedUserId, OkHttpClient client,
                           BooleanPreference mockUserFlag) {
        super(savedUserId, client);
        if (mockUserFlag.get()){
            this.user = new User("John", "Smith", "donotemail@wsj.com", true);
        }
    }

    public boolean hasUser(){ return savedUserId.isSet() || mockUserFlag.get(); `}

    public Observable<User> retrieveSavedUser(){
        return Observable.just(getUser());
    }
}
