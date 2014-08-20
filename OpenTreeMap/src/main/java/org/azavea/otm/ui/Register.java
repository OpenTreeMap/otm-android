package org.azavea.otm.ui;

import org.apache.http.Header;
import org.azavea.otm.App;
import org.azavea.otm.LoginManager;
import org.azavea.otm.R;
import org.azavea.otm.data.User;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.LoggingJsonHttpResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

public class Register extends FragmentActivity {
    private String username;
    private String password;
    private Bitmap profilePicture;
    private final LoginManager loginManager = App.getLoginManager();

    private ProgressDialog dialog;

    /*
     * Activity overrides
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);
    }

    /*
     * UI Event Handlers
     */
    public void handleRegisterClick(View view) {
        password = ((EditText) findViewById(R.id.register_password)).getText().toString();
        username = ((EditText) findViewById(R.id.register_username)).getText().toString();

        String email = ((EditText) findViewById(R.id.register_email)).getText().toString();
        String password2 = ((EditText) findViewById(R.id.register_password2)).getText().toString();
        String firstName = ((EditText) findViewById(R.id.register_firstName)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.register_lastName)).getText().toString();

        if (isEmpty(email) || isEmpty(password) || isEmpty(username)) {
            alert(R.string.all_fields_required);
        } else if (!validEmail(email)) {
            alert(R.string.invalid_email);
        } else if (!strongPassword(password)) {
            alert(R.string.new_passwords_not_strong);
        } else if (!password2.equals(password)) {
            alert(R.string.new_passwords_dont_match);
        } else {
            RequestGenerator rc = new RequestGenerator();
            User model = null;
            dialog = ProgressDialog.show(Register.this, "",
                    "Creating User Account...", true, true);

            try {
                model = new User(username, firstName, lastName, email, password);
            } catch (JSONException e) {
                Log.e("Register", "error in User JSON.");
                e.printStackTrace();
                alert(R.string.problem_creating_account);
                dialog.dismiss();
            }

            try {
                rc.register(App.getAppInstance(), model, registrationResponseHandler);
            } catch (Exception e) {
                Log.e("Register", "exception in rc.addUser");
                e.printStackTrace();
                alert(R.string.problem_creating_account);
                dialog.dismiss();
            }
        }
    }

    /*
     * Response handlers
     */
    private final JsonHttpResponseHandler registrationResponseHandler = new LoggingJsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            dialog.dismiss();
            if (responseIsSuccess(response)) {
                loginManager.logIn(App.getAppInstance(), username, password, afterLoginSendProfilePictureAndFinish);
            } else {
                Log.e("Register", response.toString());
                alert(R.string.problem_creating_account);
            }
        }

        @Override
        public void failure(Throwable e, String response) {
            dialog.dismiss();
            if (responseIsConflict(e, response)) {
                alert(R.string.username_is_taken);
            } else {
                Log.e("Register", response + "\n" + e.getMessage());
                alert(R.string.problem_creating_account);
            }
        }
    };
    private final JsonHttpResponseHandler profilePictureResponseHandler = new LoggingJsonHttpResponseHandler() {
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            if (!responseIsSuccess(response)) {
                alert(R.string.problem_setting_profile_picture);
                Log.e("Register", "problem setting profile picture");
                Log.e("Register", response.toString());
            }
            notifyUserThatAcctCreatedAndReturnToProfile();
        }

        @Override
        public void failure(Throwable e, String response) {
            alert(R.string.problem_setting_profile_picture);
            Log.e(App.LOG_TAG, "problem setting profile picture");
        }
    };
    private final Callback afterLoginSendProfilePictureAndFinish = new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Bundle data = msg.getData();
            if (data.getBoolean("success")) {
                if (null == profilePicture) {
                    notifyUserThatAcctCreatedAndReturnToProfile();
                } else {
                    sendProfilePicture();
                }
                return true;
            } else {
                alert(R.string.problem_creating_account);
                return false;
            }
        }

    };

    /*
     * Picture Activity overrides
     */
    protected void submitBitmap(Bitmap bm) {
        profilePicture = bm;
        Log.d("Register", String.format("Bitmap dimensions: %d x %d", bm.getWidth(), bm.getHeight()));
        ImageView iv = (ImageView) findViewById(R.id.register_profilePic);
        iv.setImageBitmap(profilePicture);

        // this will be submitted later, when the user sends the registration
        // request.
    }

    /*
     * Helper functions to display info to the user
     */
    private void notifyUserThatAcctCreatedAndReturnToProfile() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
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

    private void alert(int msg) {
        alert(this.getString(msg));
    }

    private void alert(String msg) {
        Toast.makeText(App.getAppInstance(), msg, Toast.LENGTH_LONG).show();
    }

    /*
     * Form validation functions
     */
    private static boolean isEmpty(String field) {
        return field.length() == 0;
    }

    private static boolean validEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private static boolean strongPassword(String password) {
        return password.length() >= 6;
    }

    private static boolean responseIsSuccess(JSONObject response) {
        String status = "";
        try {
            status = response.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return status.equals("success");
    }

    /*
     * Other helper functions
     */
    private static boolean responseIsConflict(Throwable t, String response) {
        return response.equals("CONFLICT");
    }

    private void sendProfilePicture() {
        RequestGenerator rc = new RequestGenerator();
        try {
            rc.addProfilePhoto(profilePicture, profilePictureResponseHandler);
        } catch (JSONException e) {
            alert(R.string.problem_setting_profile_picture);
            Log.e("Register", "Error formulating rc.addProfilePhoto request.");
            e.printStackTrace();
        }
    }
}
