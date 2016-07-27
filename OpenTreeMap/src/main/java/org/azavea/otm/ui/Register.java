package org.azavea.otm.ui;

import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.azavea.otm.App;
import org.azavea.otm.R;

public class Register extends FragmentActivity {
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
    public void handleContinueClick(View view) {
        String password = ((EditText) findViewById(R.id.register_password)).getText().toString();
        String username = ((EditText) findViewById(R.id.register_username)).getText().toString();

        String email = ((EditText) findViewById(R.id.register_email)).getText().toString();
        String password2 = ((EditText) findViewById(R.id.register_password2)).getText().toString();
        String firstName = ((EditText) findViewById(R.id.register_firstName)).getText().toString();
        String lastName = ((EditText) findViewById(R.id.register_lastName)).getText().toString();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password) || TextUtils.isEmpty(username)) {
            alert(R.string.all_fields_required);
        } else if (!validEmail(email)) {
            alert(R.string.invalid_email);
        } else if (!strongPassword(password)) {
            alert(R.string.new_passwords_not_strong);
        } else if (!password2.equals(password)) {
            alert(R.string.new_passwords_dont_match);
        } else {
            startActivity(TermsOfService.getIntent(this, username, email, password, firstName, lastName));
        }
    }

    private static boolean validEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private static boolean strongPassword(String password) {
        return password.length() >= 6;
    }

    private void alert(@StringRes int msg) {
        alert(this.getString(msg));
    }

    private void alert(String msg) {
        Toast.makeText(App.getAppInstance(), msg, Toast.LENGTH_LONG).show();
    }
}
