package com.example.rennt.arcticsunrise.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.example.rennt.arcticsunrise.R;
import com.example.rennt.arcticsunrise.data.api.UserManager;
import com.example.rennt.arcticsunrise.data.api.models.Issue;
import com.example.rennt.arcticsunrise.data.api.models.User;

import java.util.LinkedList;
import java.util.List;

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
 *
 * fixme: since this is a UserManager.LoggedInListener, this Presenter must be removed as a listener
 * fixme: OR kept as a singleton through dagger so that it does not leak memory.
 */
public class NavDrawerPresenter implements UserManager.LoggedInListener {
    private final ViewGroup rootNav;
    private final Context context;
    private final UserManager userManager;
    private final LayoutInflater inflater;
    private MainActivity.OnIssueChangedListener issueChangedListener;

    @InjectView(R.id.navDrawer) DrawerLayout navDrawer;
    @InjectView(R.id.banner_container) ViewGroup bannerContainer;
    @InjectView(R.id.regsiter) Button registerButton;
    @InjectView(R.id.drawerListView) ListView drawerListView;


    public NavDrawerPresenter(final ViewGroup rootView, final UserManager userManager){
        this.rootNav = rootView;
        this.context = rootView.getContext();
        this.userManager = userManager;
        inflater = LayoutInflater.from(rootView.getContext());

        ButterKnife.inject(this, rootView);

        if (userManager.isUserLoaded()){
            displayUserBanner(userManager.getUser());

        } else if (userManager.hasUser()){
            // todo: decouple instantiation of this presenter from the actual login?
            userManager.retrieveSavedUser().subscribe(new Action1<User>() {
                @Override
                public void call(User user) {
                    displayUserBanner(user);
                }
            });
        }
        userManager.addLoginListener(this);
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

    private void displayLogoutBanner(){
        ViewGroup bannerForeground = findById(bannerContainer, R.id.banner_foreground);
        bannerContainer.removeView(bannerForeground);

        inflater.inflate(R.layout.nav_drawer_banner_logged_out, bannerContainer);
        ButterKnife.inject(this, rootNav);
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
        findById(loggedInForeground, R.id.logout_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userManager.logout();
                displayLogoutBanner();
            }
        });
    }


    public void setOnIssueChangedListener(MainActivity.OnIssueChangedListener listener){
        this.issueChangedListener = listener;
    }

    public void setAvailableIssues(final List<Issue> issues){
        List<String> issueKeys = new LinkedList<>();
        for (Issue issue : issues){
            issueKeys.add(issue.getKey());
        }
        drawerListView.setAdapter(
                new ArrayAdapter<>(context, android.R.layout.simple_list_item_1, issueKeys));

        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Issue selectedIssue = issues.get(position);
                Timber.d("Selected position " + position);
                Timber.d("Changing to issue " + selectedIssue.getKey());
                issueChangedListener.onIssueChanged(selectedIssue);
                navDrawer.closeDrawers();
            }
        });
    }

    @Override
    public void onUserLoggedIn(User user) {
        displayUserBanner(user);
    }
}
