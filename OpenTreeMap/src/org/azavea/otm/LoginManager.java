package org.azavea.otm;


import org.azavea.otm.data.User;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.CBack;
import org.azavea.otm.rest.handlers.RestHandler;
import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;

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
	
	public void logIn(final String username, final String password, final Callback callback) {
		
		RequestGenerator rg = new RequestGenerator();
		rg.logIn(context, username, password, new RestHandler<User>(new User()) {
    		@Override
    		public void dataReceived(User response) {
    			prefs.edit().putString(userKey, username).commit();
    	    	prefs.edit().putString(passKey, password).commit();
    	    	
    	    	loggedInUser = response;
    	    	loggedInUser.password = password;
    	    	
    	    	Message resultMessage = new Message();
    	    	Bundle data = new Bundle();
    	    	data.putBoolean("success", true);
    	    	resultMessage.setData(data);
    	    	callback.handleMessage(resultMessage);
	    	}
    	});

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
		/*if (user != null && pass != null) {
			logIn(user, pass, new CBack() {

				@Override
				public void callBackCall() {
					//pass
					
				}
			
			});
		}*/
	}
	
	private SharedPreferences getPreferences() {
		return context.getSharedPreferences(context.getString(R.string.app_name), 
				Context.MODE_PRIVATE);
	}	
}
