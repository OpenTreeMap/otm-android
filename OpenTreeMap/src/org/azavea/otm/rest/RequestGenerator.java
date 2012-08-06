package org.azavea.otm.rest;

import java.io.UnsupportedEncodingException;

import org.azavea.otm.App;
import org.azavea.otm.LoginManager;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.User;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

public class RequestGenerator {
	private RestClient client;
	LoginManager loginManager = App.getLoginManager();
	
	public RequestGenerator() {
		client = new RestClient();
	}
	
	// Dependency injection to support unit-testing
	public void setClient(RestClient client) {
		this.client = client;
	}
	
	/**
	 * Cancel any pending or active requests started from the provided context
	 */
	public void cancelRequests(Context activityContext) {
		client.cancelRequests(activityContext);
	}
	
	public void getVersion(JsonHttpResponseHandler handler) {
		client.get("/version", null, handler);
	}
	
	public void getPlot(int id, JsonHttpResponseHandler handler) {
		client.get("/plots/" + id, null, handler);
	}
	
	public void updatePlot(Context context, int id, Plot plot, 
			AsyncHttpResponseHandler handler) throws UnsupportedEncodingException {
		if (loginManager.isLoggedIn()) {
			try {
				client.putWithAuthentication(context, "/plots/", 
						loginManager.loggedInUser.getUserName(), 
						loginManager.loggedInUser.getPassword(), id, plot, handler);
			} catch (JSONException e) {
				handleBadResponse(e);
			}
		} else {
			redirectToLoginActivity();
		}
	}

	public void addUser(Context context, User user, AsyncHttpResponseHandler handler) 
			throws JSONException, UnsupportedEncodingException {
		client.postWithAuthentication(context, "/user/", 
				loginManager.loggedInUser.getUserName(), 
				loginManager.loggedInUser.getPassword(), user, handler);
	}
	
	public void logIn(Context context, String username, String password, 
			JsonHttpResponseHandler handler) {
			client.getWithAuthentication(context, "/login", username, password, null, handler);
	}
	
	
	private void handleBadResponse(JSONException e) {
		Log.e(App.LOG_TAG, "Unable to parse JSON response", e);
	}	
	
	private void redirectToLoginActivity() {
		// TODO Auto-generated method stub
		
	}
}
