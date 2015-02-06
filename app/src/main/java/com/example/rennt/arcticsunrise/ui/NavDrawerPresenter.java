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

        final View loginDialogContent = inflater.inflate(R.layout.dialog_sign_in, rootNav, false);

        final EditText emailField = findById(loginDialogContent, R.id.email);
        final EditText passwordField = findById(loginDialogContent, R.id.password);
        TextView forgotPassword = findById(loginDialogContent, R.id.forgot_password);

        loginBuilder.setTitle("Sign in to your WSJ Account")
                .setView(loginDialogContent)
                .setPositiveButton(R.string.sign_in, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // click placeholder, see: http://stackoverflow.com/a/15619098
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        AlertDialog dialog = loginBuilder.create();
        dialog.show();
        // override button (after showing dialog) so dialog is not dismissed on click
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailField.getText().toString();
                String password = passwordField.getText().toString();
                userManager.retrieveUser(email, password).subscribe(new Action1<User>() {
                    @Override
                    public void call(User user) {
                        displayUserBanner(user);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        TextView errorMsg = findById(loginDialogContent, R.id.error_msg);
                        errorMsg.setText(throwable.getMessage());
                        errorMsg.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

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
