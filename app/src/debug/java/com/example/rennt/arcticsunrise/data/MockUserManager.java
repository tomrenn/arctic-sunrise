package com.example.rennt.arcticsunrise.data;

import com.example.rennt.arcticsunrise.data.api.SavedUserId;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.api.models.User;
import com.example.rennt.arcticsunrise.data.prefs.BooleanPreference;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.squareup.okhttp.OkHttpClient;

import rx.Observable;
import rx.Subscriber;


public class MockUserManager extends UserManager {
    private BooleanPreference mockUserFlag;

    public MockUserManager(LongPreference savedUserId, OkHttpClient client,
                           BooleanPreference mockUserFlag) {
        super(savedUserId, client);
        this.mockUserFlag = mockUserFlag;
        if (mockUserFlag.get()){
            this.user = new User("John", "Smith", "donotemail@wsj.com", true);
        }
    }

    public boolean hasUser(){
        return savedUserId.isSet() || mockUserFlag.get(); }

    public Observable<User> retrieveSavedUser(){
        // todo: actually do super if mockUserFlag is not set
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(Subscriber<? super User> subscriber) {
                subscriber.onNext(getUser());
                notifyLoginListeners(getUser());
            }
        });
    }
}
