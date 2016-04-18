package org.azavea.helpers;

import android.util.Log;

import com.rollbar.android.Rollbar;

import org.azavea.otm.App;
import org.json.JSONException;

public class Logger {
    public static void info(Throwable exception) {
        info("", exception);
    }

    public static void info(String message, Throwable exception) {
        if (Rollbar.isInit()) {
            addInfo();
            Rollbar.reportException(exception, "info", message);
        } else {
            Log.d(App.LOG_TAG, message, exception);
        }
    }

    public static void info(String message) {
        if (Rollbar.isInit()) {
            addInfo();
            Rollbar.reportMessage("info", message);
        } else {
            Log.i(App.LOG_TAG, message);
        }
    }

    public static void warning(Throwable exception) {
        warning("", exception);
    }

    public static void warning(String message, Throwable exception) {
        if (Rollbar.isInit()) {
            addInfo();
            Rollbar.reportException(exception, "warning", message);
        } else {
            Log.w(App.LOG_TAG, message, exception);
        }
    }

    public static void warning(String message) {
        if (Rollbar.isInit()) {
            addInfo();
            Rollbar.reportMessage("warning", message);
        } else {
            Log.w(App.LOG_TAG, message);
        }
    }

    public static void error(Throwable exception) {
        Logger.error("", exception);
    }

    public static void error(String message, Throwable exception) {
        if (Rollbar.isInit()) {
            addInfo();
            Rollbar.reportException(exception, "error", message);
        } else {
            Log.e(App.LOG_TAG, message, exception);
        }
    }

    public static void error(String message) {
        if (Rollbar.isInit()) {
            addInfo();
            Rollbar.reportMessage("error", message);
        } else {
            Log.e(App.LOG_TAG, message);
        }
    }

    private static void addInfo() {
        // Rollbar lets us include "person" info
        // We'll add a username, but also add in the instance url_name as the "person" id
        String urlName = null;
        String username = null;
        if (App.getCurrentInstance() != null) {
            urlName = App.getCurrentInstance().getUrlName();
        }
        if (App.getLoginManager() != null && App.getLoginManager().loggedInUser != null) {
            try {
                username = App.getLoginManager().loggedInUser.getUserName();
            } catch (JSONException e) {
                // This shouldn't happen, but if it does we'll just let username be null
            }
        }
        Rollbar.setPersonData(urlName, username, null);
    }
}
