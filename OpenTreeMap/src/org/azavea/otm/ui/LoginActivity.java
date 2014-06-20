package org.azavea.otm.ui;

import org.azavea.otm.App;
import org.azavea.otm.LoginManager;
import org.azavea.otm.R;
import org.azavea.otm.rest.handlers.RestHandler;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {
    private final LoginManager loginManager = App.getLoginManager();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        getWindow().setLayout(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
    }

    private boolean validate(String user, String pass) {
        if ("".equals(user) || "".equals(pass)) {
            String msg = "Please enter both a Username and a Password";
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void login(View view) {
        String username = ((EditText) findViewById(R.id.login_username)).getText().toString().trim();
        String password = ((EditText) findViewById(R.id.login_password)).getText().toString().trim();

        if (validate(username, password)) {
            requestLogin(username, password);
        }
    }

    private void requestLogin(String username, String password) {
        final ProgressDialog dialog = ProgressDialog.show(this, "", "Logging in...", true, true);
        loginManager.logIn(this, username, password, msg -> {
            dialog.cancel();
            Bundle data = msg.getData();
            if (data.getBoolean(RestHandler.SUCCESS_KEY)) {
                setResult(RESULT_OK);
                finish();
                return true;
            } else {
                Toast.makeText(App.getAppInstance(), data.getString("message"), Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    public void register(View view) {
        startActivity(new Intent(this, Register.class));
    }
}
