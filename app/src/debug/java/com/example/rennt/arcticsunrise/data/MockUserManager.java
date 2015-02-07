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
    }

    public boolean hasUser(){
        return savedUserId.isSet() || mockUserFlag.get();
    }

    public Observable<User> retrieveSavedUser(){
        if (mockUserFlag.get()) {
            this.user = new User("John", "Smith", "donotemail@wsj.com", true);

            return Observable.create(new Observable.OnSubscribe<User>() {
                @Override
                public void call(Subscriber<? super User> subscriber) {
                    subscriber.onNext(getUser());
                    notifyLoginListeners(getUser());
                }
            });
        } else {
            return super.retrieveSavedUser();
        }
    }
}
