package org.azavea.otm.rest;

import java.io.UnsupportedEncodingException;

import org.azavea.otm.data.Plot;
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
		client.put(context, "/plots/", id, plot, handler);
	}
}
