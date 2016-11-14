package org.azavea.otm.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.azavea.helpers.Logger;
import org.azavea.otm.App;
import org.azavea.otm.LoginManager;
import org.azavea.otm.R;
import org.azavea.otm.data.User;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.LoggingJsonHttpResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

public class TermsOfService extends Activity {

    private final LoginManager loginManager = App.getLoginManager();
    private ProgressDialog dialog;

    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String FIRST_NAME = "first_name";
    private static final String LAST_NAME = "last_name";

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        setContentView(R.layout.terms_of_service);
        WebView wv = (WebView) findViewById(R.id.terms_of_service_webview);
        wv.loadUrl("file:///android_asset/terms_of_service.html");
    }

    public static Intent getIntent(
            Context context,
            @NonNull String username, @NonNull String email, @NonNull String password,
            @NonNull String firstName, @NonNull String lastName) {
        Intent intent = new Intent(context, TermsOfService.class);
        intent.putExtra(USERNAME, username);
        intent.putExtra(EMAIL, email);
        intent.putExtra(PASSWORD, password);
        intent.putExtra(FIRST_NAME, firstName);
        intent.putExtra(LAST_NAME, lastName);

        return intent;
    }

    public void handleRegisterClick(View view) {
        RequestGenerator rc = new RequestGenerator();
        User model = null;
        dialog = ProgressDialog.show(TermsOfService.this, "", "Creating User Account...", true, true);

        Intent intent = getIntent();
        String username = intent.getStringExtra(USERNAME);
        String firstName = intent.getStringExtra(FIRST_NAME);
        String lastName = intent.getStringExtra(LAST_NAME);
        String email = intent.getStringExtra(EMAIL);
        String password = intent.getStringExtra(PASSWORD);

        try {
            model = new User(username, firstName, lastName, email, password);
        } catch (JSONException e) {
            Logger.error("error in User JSON.", e);
            showErrorAndGoBack();
            dialog.dismiss();
        }

        try {
            rc.register(App.getAppInstance(), model, registrationResponseHandler);
        } catch (Exception e) {
            Logger.error(e);
            showErrorAndGoBack();
            dialog.dismiss();
        }
    }

    /*
     * Response handlers
     */
    private final JsonHttpResponseHandler registrationResponseHandler = new LoggingJsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            dialog.dismiss();
            Intent intent = getIntent();
            String username = intent.getStringExtra(USERNAME);
            String password = intent.getStringExtra(PASSWORD);

            if (responseIsSuccess(response)) {
                loginManager.logIn(App.getAppInstance(), username, password, msg -> {
                    Bundle data = msg.getData();
                    if (data.getBoolean("success")) {
                        notifyUserThatAcctCreatedAndReturnToProfile();
                        return true;
                    } else {
                        showErrorAndGoBack();
                        return false;
                    }
                });
            } else {
                Logger.warning("Problem creating user account");
                showErrorAndGoBack();
            }
        }

        @Override
        public void failure(Throwable e, String response) {
            dialog.dismiss();
            if (responseIsConflict(e, response)) {
                Toast.makeText(TermsOfService.this, R.string.username_is_taken, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Logger.warning("Problem creating user account", e);
                showErrorAndGoBack();
            }
        }
    };

    private static boolean responseIsSuccess(JSONObject response) {
        String status = "";
        try {
            status = response.getString("status");
        } catch (JSONException e) {
            Logger.error(e);
        }
        return status.equals("success");
    }

    private static boolean responseIsConflict(Throwable t, String response) {
        return response.equals("CONFLICT");
    }

    private void showErrorAndGoBack() {
        Toast.makeText(TermsOfService.this, R.string.problem_creating_account, Toast.LENGTH_SHORT).show();
        finish();
    }

    /*
     * Helper functions to display info to the user
     */
    private void notifyUserThatAcctCreatedAndReturnToProfile() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.done_registering)
                .setMessage(R.string.done_registering_msg)
                .setPositiveButton(
                        R.string.OK,
                        (dialog1, which) -> startActivity(new Intent(App.getAppInstance(),
                                App.hasInstanceCode() ?
                                        TabLayout.class :
                                        InstanceSwitcherActivity.class
                        ))
                )
                .show();
    }
}
