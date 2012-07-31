package org.azavea.otm;


import org.azavea.otm.data.User;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;
import org.json.JSONException;
import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;

public class LoginManager {
	private final String userKey = "user";
	private final String passKey = "pass";
	private Context context;
	private SharedPreferences prefs;
	
	public User loggedInUser = null;
	
	public LoginManager(Context context) {
		this.context = context;
		this.prefs = getPreferences();
	}
		
	public boolean isLoggedIn() {
		return loggedInUser != null;
	}
	
	public void logIn(final String username, final String password, final Callback callback) {
		
		RequestGenerator rg = new RequestGenerator();
		try {
			rg.logIn(context, username, password, new RestHandler<User>(new User()) {
				@Override
				public void onFailure(Throwable e, JSONArray errorResponse){
					Log.e(App.LOG_TAG, "login bad", e);
				}
				
				@Override
				public void dataReceived(User response) {
					prefs.edit().putString(userKey, username).commit();
			    	prefs.edit().putString(passKey, password).commit();
			    	
			    	loggedInUser = response;
			    	try {
						loggedInUser.setPassword(password);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			    	Log.d(App.LOG_TAG, "Login successful: " + username);
			    	
			    	Message resultMessage = new Message();
			    	Bundle data = new Bundle();
			    	data.putBoolean("success", true);
			    	resultMessage.setData(data);
			    	callback.handleMessage(resultMessage);
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void logOut() {
		loggedInUser = null;
		prefs.edit().remove(userKey).commit();
		prefs.edit().remove(passKey).commit();
	}
	
	/** 
	 * Automatically and silently authenticate if credentials have been saved
	 */
	public void autoLogin() {
		String user = prefs.getString(userKey, null);
		String pass = prefs.getString(passKey, null);
		if (user != null && pass != null) {
			logIn(user, pass, new Callback() {
				@Override
				public boolean handleMessage(Message msg) {
					Bundle data = msg.getData();
					if(data != null && data.getBoolean("failure")) {
						logOut();
					}
					return true; 
				}
			});
		}
	}
	
	private SharedPreferences getPreferences() {
		return context.getSharedPreferences(context.getString(R.string.app_name), 
				Context.MODE_PRIVATE);
	}	
}
