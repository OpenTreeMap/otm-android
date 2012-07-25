package org.azavea.otm.rest;

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
		client.get(getAbsoluteUrl(url), reqParams, responseHandler);
	}
	
	public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		RequestParams reqParams = prepareParams(params);
		client.post(getAbsoluteUrl(url), reqParams, responseHandler);
	}
	
	public void delete(String url, AsyncHttpResponseHandler responseHandler) {
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
		//return "http://localhost:8000/api/v0.1";
		return "http://httpbin.org";
	}
	
	private String getApiKey() {
		// TODO: Expand once authentication management has been implemented
		return "123456";
	}
	
	private String getAbsoluteUrl(String relativeUrl) {
		return baseUrl + relativeUrl;
	}
}
