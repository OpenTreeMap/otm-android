package org.azavea.helpers;

import android.util.Log;

import com.rollbar.android.Rollbar;

import org.azavea.otm.App;

public class Logger {
    public static void info(Throwable exception) {
        info("", exception);
    }

    public static void info(String message, Throwable exception) {
        if (Rollbar.isInit()) {
            Rollbar.reportException(exception, "info", message);
        } else {
            Log.d(App.LOG_TAG, message, exception);
        }
    }

    public static void info(String message) {
        if (Rollbar.isInit()) {
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
            Rollbar.reportException(exception, "warning", message);
        } else {
            Log.w(App.LOG_TAG, message, exception);
        }
    }

    public static void warning(String message) {
        if (Rollbar.isInit()) {
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
            Rollbar.reportException(exception, "error", message);
        } else {
            Log.e(App.LOG_TAG, message, exception);
        }
    }

    public static void error(String message) {
        if (Rollbar.isInit()) {
            Rollbar.reportMessage("error", message);
        } else {
            Log.e(App.LOG_TAG, message);
        }
    }
}
