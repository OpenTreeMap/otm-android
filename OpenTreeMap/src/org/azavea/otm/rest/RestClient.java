package org.azavea.otm.rest;

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.azavea.otm.data.Model;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.*;

// This class is designed to take care of the base-url
// and otm api-key for REST requests
public class RestClient {
	private String baseUrl;
	
	private String apiKey;
	
	private AsyncHttpClient client;
	
	
	public RestClient() {
		baseUrl = getBaseUrl();
		apiKey = getApiKey();
		client = new AsyncHttpClient();
	}

	// Dependency injection to support mocking
	// in unit-tests
	public void setAsyncClient(AsyncHttpClient client) {
		this.client = client;
	}
	
	public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		RequestParams reqParams = prepareParams(params);
		Log.d("rc", "Sending get request...");
		client.get(getAbsoluteUrl(url), reqParams, responseHandler);
	}
	
	public void post(Context context, String url, int id, Model model, AsyncHttpResponseHandler response) throws UnsupportedEncodingException {
		//client.post(getAbsoluteUrl(url), reqParams, responseHandler);
		String completeUrl = getAbsoluteUrl(url);
		completeUrl += id + "?apikey=" + getApiKey();
		client.setBasicAuth("administrator", "123456");
		client.post(context, completeUrl, new StringEntity(model.getData().toString()), "application/json", response);
	}
	
	public void put(Context context, String url, int id, Model model, AsyncHttpResponseHandler response) throws UnsupportedEncodingException {
		String completeUrl = getAbsoluteUrl(url);
		completeUrl += id + "?apikey=" + getApiKey();
		client.setBasicAuth("administrator", "123456");
		client.put(context, completeUrl, new StringEntity(model.getData().toString()), "application/json", response);
	}
	
	public void delete(String url, AsyncHttpResponseHandler responseHandler) {
		client.setBasicAuth("administrator", "123456");
		client.delete(getAbsoluteUrl(url), responseHandler);
	}
	
	private RequestParams prepareParams(RequestParams params) {
		// We'll always need a RequestParams object since we'll always
		// be sending an apikey
		RequestParams reqParams;
		if (params == null) {
			reqParams = new RequestParams();
		} else {
			reqParams = params;
		}
		
		reqParams.put("apikey", apiKey);
		
		return reqParams;
	}
	
	private String getBaseUrl() {
		// TODO: Expand once configuration management has been implemented
		return "http://10.0.2.2:8000/api/v0.1";
		//return "http://httpbin.org";
	}
	
	private String getApiKey() {
		// TODO: Expand once authentication management has been implemented
		return "123456";
	}
	
	private String getAbsoluteUrl(String relativeUrl) {
		return baseUrl + relativeUrl;
	}
}
