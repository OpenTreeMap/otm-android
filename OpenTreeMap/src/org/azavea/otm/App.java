package org.azavea.otm;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.azavea.lists.NearbyList;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;

/**
 * A global singleton object to maintain application state
 */
public class App extends Application {
    public static final String LOG_TAG = "AZ_OTM";
    public static final String INSTANCE_CODE = "instance_code";

    private static App appInstance = null;
    private static LoginManager loginManager = null;
    private static FilterManager filterManager = null;
    private static FieldManager fieldManager = null;
    private static NearbyList nearbyList = null;

    private static SharedPreferences sharedPreferences = null;
    private static boolean pendingEnabled = false;
    private static InstanceInfo currentInstance;
    private static boolean loadingInstance = false;

    private static AsyncHttpClient asyncHttpClient;
    private static ArrayList<Callback> registeredInstanceCallbacks = new ArrayList<Callback>();

    public static App getAppInstance() {
        checkAppInstance();
        return appInstance;
    }

    public static LoginManager getLoginManager() {
        if (loginManager == null) {
            checkAppInstance();
            loginManager = new LoginManager(appInstance);
            loginManager.autoLogin();
        }
        return loginManager;
    }

    /**
     * Static access to single search manager instance
     */
    public static FilterManager getFilterManager() {
        return filterManager;
    }

    /**
     * Static access to single field manager instance
     */
    public static FieldManager getFieldManager() {
        return fieldManager;
    }

    public static SharedPreferences getSharedPreferences() {
        if (sharedPreferences == null) {
            checkAppInstance();
            sharedPreferences = appInstance.getSharedPreferences(appInstance.getString(R.string.app_name), Context.MODE_PRIVATE);
            // Set-up SharedPreferences if they haven't been set up before
            setDefaultSharedPreferences(sharedPreferences);
        }
        return sharedPreferences;
    }

    public static boolean isPendingEnabled() {
        return pendingEnabled;
    }

    private static void checkAppInstance() {
        if (appInstance == null) {
            throw new IllegalStateException("Application not created yet");
        }
    }

    // (re)Load the relevant resources into the
    // applications shared-preferences.
    private static void setDefaultSharedPreferences(SharedPreferences prefs) {
        Editor editor = prefs.edit();
        Context context = appInstance.getApplicationContext();
        editor.putString("base_url", context.getString(R.string.base_url))
              .putString("api_url", context.getString(R.string.api_url))
              .putString("tiler_url", context.getString(R.string.tiler_url))
              .putString("plot_feature", context.getString(R.string.plot_feature))
              .putString("boundary_feature", context.getString(R.string.boundary_feature))
              .putString("access_key", context.getString(R.string.access_key))
              .putString("secret_key", context.getString(R.string.secret_key))
              .putString("max_nearby_plots", context.getString(R.string.max_nearby_plots))
              .putString("starting_zoom_level", context.getString(R.string.starting_zoom_level))
              .commit();

    }

    private static void loadPendingStatus() {
        // Load the pending setting from the included XML resource
        InputStream filterFile = App.getAppInstance().getResources().openRawResource(R.raw.configuration);
        try {
            DocumentBuilder xml = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = xml.parse(filterFile);
            NodeList pending = doc.getElementsByTagName("pending");
            if (pending.getLength() > 0) {
                Node el = pending.item(0);
                pendingEnabled = Boolean.parseBoolean(el.getTextContent());
            }
        } catch (Exception e) {
            pendingEnabled = false;
            Log.e(LOG_TAG, "Invalid pending configuration xml file", e);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;

        // Create an instance of login manager immediately, so that
        // the app can try to auto log in on any saved credentials
        getLoginManager();
        loadPendingStatus();
    }

    public static AsyncHttpClient getAsyncHttpClient() {
        if (asyncHttpClient == null) {
            asyncHttpClient = new AsyncHttpClient();
        }

        return asyncHttpClient;
    }

    public static NearbyList getNearbyList(Context context) {
        if (nearbyList == null) {
            nearbyList = new NearbyList(context );
        }

        return nearbyList;
    }

    /**
     * INSTANCE API
     * <p/>
     * TODO: Move to another class.
     */


    public static String getInstanceName() {
        App appInstance = getAppInstance();
        InstanceInfo currentInstance = appInstance.getCurrentInstance();
        return currentInstance == null ?
                appInstance.getString(R.string.app_name) : currentInstance.getName();
    }


    public static class InstanceRefreshHandler extends RestHandler<InstanceInfo> {
        private Callback callback;
        private InstanceInfo responseInstanceInfo;

        public InstanceRefreshHandler(Callback callback) {
            this(new InstanceInfo(), callback);
        }

        public InstanceRefreshHandler() {
            this(new InstanceInfo(), null);
        }

        public InstanceRefreshHandler(InstanceInfo instanceInfo, Callback callback) {
            super(instanceInfo);
            this.responseInstanceInfo = instanceInfo;
            this.callback = callback;
        }

        private void handleRegisteredCallbacks(Message msg) {
            if (registeredInstanceCallbacks.size() > 0) {
                for (Callback registeredCallback : registeredInstanceCallbacks) {
                    registeredCallback.handleMessage(msg);
                }
                registeredInstanceCallbacks.clear();
            }
        }

        private void handleCallback(boolean success) {
            Message msg = new Message();
            Bundle data = new Bundle();
            data.putBoolean("success", success);
            msg.setData(data);

            // Flag to track if instance request is pending
            loadingInstance = false;

            if (callback != null) {
                callback.handleMessage(msg);
            }

            // In addition to the direct caller, other callbacks may have been
            // registered to be notified of instance loaded status
            handleRegisteredCallbacks(msg);
        }

        @Override
        public void onFailure(Throwable e, String message) {
            Log.e(App.LOG_TAG, "Unable to Load Instance: " + responseInstanceInfo.getName(), e);
            Toast.makeText(appInstance, "Cannot load configured instance.",
                    Toast.LENGTH_LONG).show();
            handleCallback(false);
        }

        @Override
        public void dataReceived(InstanceInfo instanceInfo) {
            setCurrentInstance(instanceInfo);
            handleCallback(true);
        }
    }

    public static InstanceInfo getCurrentInstance() {
        return currentInstance;
    }

    public static boolean hasSkinCode() {
        return !TextUtils.isEmpty(appInstance.getString(R.string.instance_code));
    }

    public static boolean hasInstanceCode() {
        return hasSkinCode() || getSharedPreferences().contains(INSTANCE_CODE);
    }

    public static void removeCurrentInstance() {
        currentInstance = null;
        getSharedPreferences().edit().remove(INSTANCE_CODE).commit();
        reloadInstanceInfo(null);
    }

    /**
     * Clear the current instance info and force a reload of the configured instance
     *
     * @param callback to call when instance is loaded
     */
    public static void reloadInstanceInfo(String instanceCode, Callback callback) {
        currentInstance = null;
        loadingInstance = true;
        // unnecessary if there is a hard coded instance, can't
        RequestGenerator rg = new RequestGenerator();
        InstanceRefreshHandler handler = new InstanceRefreshHandler(callback);
        rg.getInstanceInfo(instanceCode, handler);
    }

    public static void reloadInstanceInfo(Callback callback) {
        String hardCodedInstanceCode = appInstance.getString(R.string.instance_code);
        String instanceCode;
        if (!TextUtils.isEmpty(hardCodedInstanceCode)) {
            instanceCode = hardCodedInstanceCode;
        } else if (getSharedPreferences().contains(INSTANCE_CODE)) {
            String neverUsed = "";
            instanceCode = getSharedPreferences().getString(INSTANCE_CODE, neverUsed);
        } else {
            // TODO: reprompt the instance switcher
            Log.e(LOG_TAG, "NOT IMPLEMENTED YET");
            return;
        }
        reloadInstanceInfo(instanceCode, callback);
    }

    /**
     * Callback to ensure the instance has been loaded, either via a loaded, pending
     * or missing instance info. This method is safe to call at any time to wait for
     * instance info before proceeding with the callback.
     *
     * @param callback
     */
    public void ensureInstanceLoaded(final Callback callback) {
        if (currentInstance != null) {
            Message msg = Message.obtain();
            Bundle data = new Bundle();
            data.putBoolean("success", true);
            msg.setData(data);

            callback.handleMessage(msg);
        } else {
            // If an instance request is pending, register for a callback on completion,
            // otherwise, force an instance request
            if (loadingInstance) {
                registeredInstanceCallbacks.add(callback);
            } else {
                reloadInstanceInfo(callback);
            }
        }
    }


    /**
     * Given the provided instance, create fields and filters
     */
    private static void setCurrentInstance(InstanceInfo currentInstance) {
        App.currentInstance = currentInstance;
        getSharedPreferences().edit().putString(INSTANCE_CODE, currentInstance.getUrlName()).commit();

        try {
            fieldManager = new FieldManager(currentInstance);

            filterManager = new FilterManager(currentInstance.getSearchDefinitions());

        } catch (Exception e) {
            Log.e(LOG_TAG, "Unable to create field manager from instance", e);
            Toast.makeText(appInstance, "Error setting up OpenTreeMap",
                    Toast.LENGTH_LONG).show();
        }
    }


}
