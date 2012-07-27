package org.azavea.otm.rest;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.Context;
import android.util.Base64;
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
	
	/**
	 * Executes a get request and adds basic authentication headers to the request.
	 */
	public void getWithAuthentication(Context context, String url, String username, 
			String password, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		RequestParams reqParams = prepareParams(params);
		Header[] headers = {createBasicAuthenticationHeader(username, password)};
		client.get(context, getAbsoluteUrl(url), headers, reqParams, responseHandler);
	}
	
	public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		RequestParams reqParams = prepareParams(params);
		client.post(getAbsoluteUrl(url), reqParams, responseHandler);
	}
	
	/**
	 * Executes a post request and adds basic authentication headers to the request.
	 */
	public void postWithAuthentication(Context context, String url, String username, 
			String password, RequestParams params, String contentType, 
			AsyncHttpResponseHandler responseHandler) {
		RequestParams reqParams = prepareParams(params);
		Header[] headers = {createBasicAuthenticationHeader(username, password)};
		client.post(context, getAbsoluteUrl(url), headers, reqParams, contentType, 
				responseHandler);
	}

	public void delete(String url, AsyncHttpResponseHandler responseHandler) {
		client.delete(getAbsoluteUrl(url), responseHandler);
	}

	/**
	 * Executes a delete request and adds basic authentication headers to the request.
	 */
	public void deleteWithAuthentication(Context context, String url, String username, 
			String password, AsyncHttpResponseHandler responseHandler) {
		Header[] headers = {createBasicAuthenticationHeader(username, password)};
		client.delete(context, getAbsoluteUrl(url), headers, responseHandler);
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
	
	private Header createBasicAuthenticationHeader(String username, String password) {
		String credentials = String.format("%s:%s", username, password);
		String encoded = Base64.encodeToString(credentials.getBytes(), Base64.DEFAULT);
		return new BasicHeader("Authorization", String.format("%s %s", "Basic", encoded));
	}
}
