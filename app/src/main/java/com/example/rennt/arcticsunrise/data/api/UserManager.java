package com.example.rennt.arcticsunrise.data.api;

import com.example.rennt.arcticsunrise.data.api.models.User;
import com.example.rennt.arcticsunrise.data.prefs.LongPreference;
import com.squareup.okhttp.OkHttpClient;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.OnErrorNotImplementedException;
import rx.schedulers.Schedulers;


public class UserManager {
    private List<LoggedInListener> inListeners;
    private List<LoggedOutListener> outListeners;
    protected LongPreference savedUserId;
    private OkHttpClient httpClient;
    protected User user;

    public interface LoggedInListener {
        public void onUserLoggedIn(User user);
    }

    public interface LoggedOutListener {
        public void onUserLoggedOut();
    }

    public UserManager(@SavedUserId LongPreference savedUserId, OkHttpClient client){
        this.savedUserId = savedUserId;
        this.httpClient = client;
        this.inListeners = new LinkedList<>();
        this.outListeners = new LinkedList<>();
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

    public void logout(){
        this.user = null;
        savedUserId.delete();
        notifyLogoutListeners();
    }

    protected void notifyLoginListeners(User user){
        for (LoggedInListener inListener : inListeners) {
            inListener.onUserLoggedIn(user);
        }
    }

    protected void notifyLogoutListeners(){
        for (LoggedOutListener outListener : outListeners) {
            outListener.onUserLoggedOut();
        }
    }


    public void addLoginListener(LoggedInListener inListener){
        this.inListeners.add(inListener);
    }

    public void removeLoginListener(LoggedInListener inListener){
        this.inListeners.remove(inListener);
    }

    public void addLogoutListener(LoggedOutListener outListener){
        this.outListeners.add(outListener);
    }

    public void removeLogoutListener(LoggedOutListener outListener){
        this.outListeners.remove(outListener);
    }

    public void addListeners(LoggedInListener inListener, LoggedOutListener outListener){
        addLoginListener(inListener);
        addLogoutListener(outListener);
    }


}
