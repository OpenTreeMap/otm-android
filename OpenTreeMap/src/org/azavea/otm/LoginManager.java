package org.azavea.otm;

import org.azavea.otm.data.User;

import android.content.Context;
import android.content.SharedPreferences;

public class LoginManager {
	private final String userKey = "user";
	private final String passKey = "pass";
	private Context context;
	private SharedPreferences prefs;
	
	private boolean loggedIn = false;
	
	public User loggedInUser = null;
	
	public LoginManager(Context context) {
		this.context = context;
		this.prefs = getPreferences();
		autoLogin();
	}
		
	public boolean isLoggedIn() {
		return loggedIn;
	}
	
	public boolean logIn(String username, String password) {
		// TODO: api call login
		
    	prefs.edit().putString(userKey, username).commit();
    	prefs.edit().putString(passKey, password).commit();
    	
    	// TODO: delete temp user construction
    	loggedInUser = new User();
    	loggedInUser.username=username;
    	loggedInUser.password=password;
    	return true;
	}

	public void logOut() {
		loggedInUser = null;
		prefs.edit().remove(userKey).commit();
		prefs.edit().remove(passKey).commit();
	}
	
	/** 
	 * Automatically authenticate if credentials have been saved
	 */
	private void autoLogin() {
		String user = prefs.getString(userKey, null);
		String pass = prefs.getString(passKey, null);
		if (user != null && pass != null) {
			logIn(user, pass);
		}
	}
	
	private SharedPreferences getPreferences() {
		return context.getSharedPreferences(context.getString(R.string.app_name), 
				Context.MODE_PRIVATE);
	}	
}
