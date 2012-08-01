package org.azavea.otm;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * A global singleton object to maintain application state
 */
public class App extends Application {
	public static final String LOG_TAG = "AZ_OTM";
	
	private static App instance = null;
	private static LoginManager loginManager = null;
	private static SharedPreferences sharedPreferences = null;
	
	public static App getInstance() {
		checkInstance();
		return instance;
	}
	
	public static LoginManager getLoginManager() {
		if (loginManager == null) {
			checkInstance();
			loginManager = new LoginManager(instance);
			loginManager.autoLogin();
		}
		return loginManager;
	}

	public static SharedPreferences getSharedPreferences() {
		if (sharedPreferences == null) {
			checkInstance();
			sharedPreferences = instance.getSharedPreferences(instance.getString(R.string.app_name), Context.MODE_PRIVATE);
			// Set-up SharedPreferences iff they haven't been set up before
			setDefaultSharedPreferences(sharedPreferences);
		}
		return sharedPreferences;
	}
	
	private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet");
    }
	
	// Check to see if this has run before.
	// If it hasn't, load the relevant resources into the
	// applications shared-preferences.
	private static void setDefaultSharedPreferences(SharedPreferences prefs) {
		boolean firstRun = prefs.getBoolean("first_run", true);
		
		if (firstRun) {
			Editor editor = prefs.edit();
			Context context = instance.getApplicationContext();
			editor.putBoolean("first_run", false)
				  .putString("base_url", context.getString(R.string.base_url))
				  .putString("api_key", context.getString(R.string.api_key))
				  .commit();
		}
	}
	
	@Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        // Create an instance of login manager immediately, so that
        // the app can try to auto log in on any saved credentials
        getLoginManager();
    }	 
}
