package com.example.rennt.arcticsunrise.data.api;

import com.example.rennt.arcticsunrise.data.api.models.User;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.squareup.okhttp.OkHttpClient;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorNotImplementedException;
import rx.schedulers.Schedulers;


public class UserManager {
    protected LongPreference savedUserId;
    private OkHttpClient httpClient;
    protected User user;


    public UserManager(@SavedUserId LongPreference savedUserId, OkHttpClient client){
        this.savedUserId = savedUserId;
        this.httpClient = client;
    }

    // todo: figure out if this should use savedUserId.isSet(),
    public boolean hasUser(){ return savedUserId.isSet(); }

    public User getUser(){ return this.user; }

    // if the user obj has already been loaded
    public boolean isUserLoaded() { return this.user != null; }


    // todo: make login event that would update stuff
    public Observable<User> retrieveSavedUser(){
        // todo: create observable that uses the saved user preference
        return null;
    }

    // create uuid, implement OAUTH, fetch from network
    public Observable<User> retrieveUser(final String email, final String password) {
        return Observable.create(new Observable.OnSubscribe<User>(){

            @Override
            public void call(Subscriber<? super User> subscriber) {
                subscriber.onError(new UnsupportedOperationException("Login not implemented"));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
