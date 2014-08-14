package org.azavea.otm.ui;

import java.io.UnsupportedEncodingException;

import org.azavea.otm.App;
import org.azavea.otm.LoginManager;
import org.azavea.otm.R;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

public class ChangePassword extends UpEnabledActionBarActivity {
    private String newPassword;
    LoginManager loginManager = App.getLoginManager();

    private JsonHttpResponseHandler changePasswordResponseHandler = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(JSONObject response) {
            try {
                if (response.has("username")) {
                    loginManager.loggedInUser.setPassword(newPassword);
                    loginManager.storePassword(newPassword);
                    notifyUserPasswordChangedAndFinish();
                } else {
                    alert(R.string.password_change_error);
                }
            } catch (JSONException e) {
                Log.e(App.LOG_TAG, "Error changing password", e);
                alert(R.string.password_change_error);
            }
        }

        @Override
        public void onFailure(Throwable e, JSONObject errorResponse) {
            Log.e(App.LOG_TAG, "Error changing password", e);
            alert(R.string.password_change_error);
        }

        @Override
        protected void handleFailureMessage(Throwable e, String responseBody) {
            Log.e(App.LOG_TAG, "Error changing password", e);
            alert(R.string.password_change_error);
        }

    };

    private void notifyUserPasswordChangedAndFinish() {
        final Activity thisActivity = this;
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.password_changed)
                .setMessage(R.string.password_changed)
                .setPositiveButton(R.string.OK, (dialog, which) -> thisActivity.finish()).show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_password);
    }

    private boolean newPasswordsMatch() {
        String p1 = ((EditText) findViewById(R.id.newPassword1)).getText().toString();
        String p2 = ((EditText) findViewById(R.id.newPassword2)).getText().toString();
        return p1.equals(p2);
    }

    private boolean newPasswordMeetsStandard() {
        Editable p1 = ((EditText) findViewById(R.id.newPassword1)).getText();
        return p1.length() >= 6;
    }

    private boolean oldPasswordIsBlank() {
        Editable old = ((EditText) findViewById(R.id.oldPassword)).getText();
        return (old.length() == 0);

    }

    private boolean newPasswordIsBlank() {
        Editable n = ((EditText) findViewById(R.id.newPassword1)).getText();
        return (n.length() == 0);
    }

    private boolean authorizedToChangePassword() throws JSONException {
        LoginManager loginManager = App.getLoginManager();
        String p = loginManager.loggedInUser.getPassword();
        String pValidate = ((EditText) findViewById(R.id.oldPassword)).getText().toString();
        return p.equals(pValidate);
    }

    private void alert(int msg) {
        String s = this.getString(msg);
        Toast.makeText(App.getAppInstance(), s, Toast.LENGTH_LONG).show();
    }

    private void changePassword() throws UnsupportedEncodingException, JSONException {
        newPassword = ((EditText) findViewById(R.id.newPassword1)).getText().toString();
        RequestGenerator rc = new RequestGenerator();
        rc.changePassword(App.getAppInstance(), newPassword, changePasswordResponseHandler);
    }

    public void handleChangePasswordClick(View view) {
        boolean authorizedToChangePassword;
        try {
            authorizedToChangePassword = authorizedToChangePassword();
        } catch (JSONException e) {
            Log.e("PasswordChange", "exception checking current password");
            Log.e("PasswordChange", e.toString());
            alert(R.string.password_change_error);
            return;
        }

        if (oldPasswordIsBlank()) {
            alert(R.string.no_blank_old_password);
        } else if (newPasswordIsBlank()) {
            alert(R.string.no_blank_new_password);
        } else if (!authorizedToChangePassword) {
            alert(R.string.bad_old_password);
        } else if (!newPasswordsMatch()) {
            alert(R.string.new_passwords_dont_match);
        } else if (!newPasswordMeetsStandard()) {
            alert(R.string.new_passwords_not_strong);
        } else {
            try {
                changePassword();
            } catch (Exception e) {
                Log.e("ChangePassword", "exception generating request");
                Log.e("ChangePassword", e.toString());
                alert(R.string.password_change_error);
            }
        }
    }
}
