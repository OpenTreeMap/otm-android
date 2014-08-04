package org.azavea.otm;

import java.net.ConnectException;

import org.azavea.otm.data.User;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;
import org.azavea.otm.ui.InstanceSwitcherActivity;
import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

public class LoginManager {
    private static final String MESSAGE_KEY = "message";
    private static final String USER_KEY = "user";
    private static final String PASS_KEY = "pass";

    private final Context context;
    private final SharedPreferences prefs;

    public User loggedInUser = null;

    public LoginManager(Context context) {
        this.context = context;
        this.prefs = getPreferences();
    }

    /**
     * Store new password in user preferences
     */
    public void storePassword(String newpass) {
        prefs.edit().putString(PASS_KEY, newpass).commit();
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public void logIn(final Context activityContext, final String username, final String password,
                      final Callback callback) {

        final RequestGenerator rg = new RequestGenerator();

        rg.logIn(activityContext, username, password, new RestHandler<User>(new User()) {

            Message resultMessage = new Message();
            Bundle data = new Bundle();

            private void handleCallback(Bundle resp) {
                resultMessage.setData(data);

                if (App.hasInstanceCode()) {
                    App.reloadInstanceInfo(new Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            callback.handleMessage(resultMessage);
                            return true;
                        }
                    });
                } else {
                    callback.handleMessage(resultMessage);
                }
            }

            @Override
            public void onFailure(Throwable e, String message) {
                if (e instanceof ConnectException) {
                    Log.e(App.LOG_TAG, "timeout");
                    data.putBoolean(SUCCESS_KEY, false);
                    data.putString(MESSAGE_KEY, "Could not connect to server");
                    handleCallback(data);
                }
                rg.cancelRequests(activityContext);
            }

            @Override
            public void handleFailureMessage(Throwable e, String responseBody) {
                data.putBoolean(SUCCESS_KEY, false);
                data.putString(MESSAGE_KEY, "Error logging in, please check your username and password.");
                handleCallback(data);
            }

            @Override
            public void dataReceived(User response) {
                prefs.edit().putString(USER_KEY, username).commit();
                storePassword(password);

                loggedInUser = response;
                try {
                    loggedInUser.setPassword(password);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                data.putBoolean(SUCCESS_KEY, true);
                handleCallback(data);
            }
        });
    }

    public void logOut(Activity activity) {
        logOut();
        if (!App.hasSkinCode()) {
            Intent intent = new Intent(activity, InstanceSwitcherActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.startActivity(intent);
            activity.finish();
        }
    }

    private void logOut() {
        // Previous instance info is now invalid
        App.removeCurrentInstance();

        loggedInUser = null;
        prefs.edit().remove(USER_KEY).commit();
        prefs.edit().remove(PASS_KEY).commit();
    }

    /**
     * Automatically and silently authenticate if credentials have been saved
     */
    public void autoLogin(final Callback callback) {
        String user = prefs.getString(USER_KEY, null);
        String pass = prefs.getString(PASS_KEY, null);
        if (user != null && pass != null) {
            logIn(context, user, pass, new Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    Bundle data = msg.getData();
                    if (data == null || !data.getBoolean(RestHandler.SUCCESS_KEY)) {
                        logOut();
                    }
                    return callback.handleMessage(msg);
                }
            });
        } else if (App.hasInstanceCode()) {
            // If there is no user to auto login, the app still needs to
            // make an instance request, which is otherwise associated and
            // instigated after a login attempt
            App.reloadInstanceInfo(callback);
        } else {
            Message msg = Message.obtain();
            Bundle args = new Bundle();
            args.putBoolean(RestHandler.SUCCESS_KEY, false);
            msg.setData(args);

            callback.handleMessage(msg);
        }
    }

    private SharedPreferences getPreferences() {
        return context.getSharedPreferences(context.getString(R.string.app_name), Context.MODE_PRIVATE);
    }
}
