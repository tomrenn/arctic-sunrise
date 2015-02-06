package com.example.rennt.arcticsunrise.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.api.models.User;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import timber.log.Timber;

import static butterknife.ButterKnife.findById;

/**
 * Navigation drawer presenter.
 * - Handle user state banner. (Logged out v.s. Logged in)
 * - Handle click actions in nav menu.
 */
public class NavDrawerPresenter {
    private final ViewGroup rootNav;
    private final Context context;
    private final UserManager userManager;
    private final LayoutInflater inflater;

    @InjectView(R.id.banner_container) ViewGroup bannerContainer;
    @InjectView(R.id.sign_in) Button signInButton;
    @InjectView(R.id.regsiter) Button registerButton;

    public NavDrawerPresenter(final ViewGroup rootView, final UserManager userManager){
        this.rootNav = rootView;
        this.context = rootView.getContext();
        this.userManager = userManager;
        inflater = LayoutInflater.from(rootView.getContext());
        // setup on click buttons, etc.
        ButterKnife.inject(this, rootView);

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
        // todo: login button must have ability to display a login dialog with it's own presenter
    }

    @OnClick(R.id.sign_in) public void showLoginDialog(){
        AlertDialog.Builder loginBuilder = new AlertDialog.Builder(rootNav.getContext());

        final EditText emailField = new EditText(context);
        emailField.setHint("Email Address");
        EditText passwordField = new EditText(context);
        passwordField.setInputType(EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout fieldContainer = new LinearLayout(context);
        fieldContainer.setOrientation(LinearLayout.VERTICAL);
        fieldContainer.addView(emailField);
        fieldContainer.addView(passwordField);

        loginBuilder.setTitle("Sign in to your WSJ Account")
                .setView(fieldContainer)
                .setPositiveButton(R.string.sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // todo: call UserManager login observable
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                })
                .show();
    }

    public void displayUserBanner(User user){
        // remove signIn, Registration buttons
        ViewGroup bannerForeground = findById(bannerContainer, R.id.banner_foreground);
        bannerContainer.removeView(bannerForeground);
        // add user info, logout button
        View loggedInForeground = inflater.inflate(
                R.layout.nav_drawer_banner_logged_in, bannerContainer);
        TextView nameLabel = findById(loggedInForeground, R.id.user_name);
        TextView emailLabel = findById(loggedInForeground, R.id.user_email);
        nameLabel.setText(user.getFullName());
        emailLabel.setText(user.getEmail());
    }
}
