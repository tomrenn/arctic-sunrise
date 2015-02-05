package com.example.rennt.arcticsunrise.ui;

import android.view.View;
import android.widget.Button;

import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.api.models.User;

import butterknife.ButterKnife;
import butterknife.InjectView;
import rx.functions.Action1;

/**
 * Navigation drawer presenter.
 * - Handle user state banner. (Logged out v.s. Logged in)
 * - Handle click actions in nav menu.
 */
public class NavDrawerPresenter {
    private final View rootNav;
    private final UserManager userManager;

    @InjectView(R.id.sign_in) Button signInButton;

    public NavDrawerPresenter(final View rootView, final UserManager userManager){
        this.rootNav = rootView;
        this.userManager = userManager;
        if (userManager.isUserLoaded()){
            displayUserBanner(userManager.getUser());
        }
        if (userManager.hasUser()){
            userManager.retrieveSavedUser().subscribe(new Action1<User>() {
                @Override
                public void call(User user) {
                    displayUserBanner(user);
                }
            });
        }

        // setup on click buttons, etc.
        ButterKnife.inject(this, rootView);
        // todo: login button must have ability to display a login dialog with it's own presenter`
    }

    public void displayUserBanner(User user){
        // remove signIn, Registration buttons

        // add user info, logout button
    }
}
