package org.azavea.otm;

import android.app.Application;

/**
 * A global singleton object to maintain application state
 */
public class App extends Application {
	public static final String LOG_TAG = "AZ_OTM";
	
	private static App instance = null;
	private static LoginManager loginManager = null;
	
	public static App getInstance() {
		checkInstance();
		return instance;
	}
	
	public static LoginManager getLoginManager() {
		if (loginManager == null) {
			checkInstance();
			loginManager = new LoginManager(instance);
		}
		return loginManager;
	}
	
	private static void checkInstance() {
        if (instance == null)
            throw new IllegalStateException("Application not created yet");
    }
	 
	@Override
    public void onCreate() {
        super.onCreate();
        instance = this;
    }	 
}
