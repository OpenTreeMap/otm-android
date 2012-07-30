package org.azavea.otm.rest;

import java.io.UnsupportedEncodingException;

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
	
	public RequestGenerator() {
		client = new RestClient();
	}
	
	// Dependency injection to support unit-testing
	public void setClient(RestClient client) {
		this.client = client;
	}
	
	public void getVersion(JsonHttpResponseHandler handler) {
		client.get("/version", null, handler);
	}
	
	public void getPlot(int id, JsonHttpResponseHandler handler) {
		client.get("/plots/" + id, null, handler);
	}
	
	public void updatePlot(Context context, int id, Plot plot, AsyncHttpResponseHandler handler) throws UnsupportedEncodingException {
		client.putWithAuthentication(context, "/plots/", "administrator", "123456", id, plot, handler);
	}
	
	public void addUser(Context context, User user, AsyncHttpResponseHandler handler) throws JSONException, UnsupportedEncodingException {
		client.postWithAuthentication(context, "/user/", "administrator", "123456", user, handler);
	}
}
