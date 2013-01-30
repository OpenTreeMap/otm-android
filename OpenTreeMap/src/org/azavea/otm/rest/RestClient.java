package org.azavea.otm.rest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Model;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.Base64;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

// This class is designed to take care of the base-url
// and otm api-key for REST requests
public class RestClient {
	private String baseUrl;
	
	private String apiKey;
	
	private AsyncHttpClient client;
	
	private SharedPreferences prefs;
	
	private String appVersion;
	
	public RestClient() {
		prefs = App.getSharedPreferences();
		baseUrl = getBaseUrl();
		apiKey = getApiKey();
		appVersion = getAppVersion();
		client = createHttpClient();
	}

	// Dependency injection to support mocking
	// in unit-tests
	public void setAsyncClient(AsyncHttpClient client) {
		this.client = client;
	}
	
	public void cancelRequests(Context context) {
		client.cancelRequests(context, true);
	}
	
	public void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		RequestParams reqParams = prepareParams(params);
		Log.d("rc", baseUrl);
		client.get(getAbsoluteUrl(url), reqParams, responseHandler);
	}
	
	public void post(Context context, String url, int id, Model model, AsyncHttpResponseHandler response) throws UnsupportedEncodingException {
		String completeUrl = getAbsoluteUrl(url) + id;
		completeUrl = prepareUrl(completeUrl);
		client.post(context, completeUrl, new StringEntity(model.getData().toString()), "application/json", response);
	}

	public void post(Context context, String url, Model model, AsyncHttpResponseHandler response) throws UnsupportedEncodingException {
		String completeUrl = getAbsoluteUrl(url);
		completeUrl = prepareUrl(completeUrl);
		client.post(context, completeUrl, new StringEntity(model.getData().toString()), "application/json", response);
	}

	
	public void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
		RequestParams reqParams = prepareParams(params);
		client.post(getAbsoluteUrl(url), reqParams, responseHandler);
	}
	
	
	
	public void put(Context context, String url, int id, Model model, 
			AsyncHttpResponseHandler response) throws UnsupportedEncodingException {
		String completeUrl = getAbsoluteUrl(url) + id;
		completeUrl = prepareUrl(completeUrl);
		client.put(context, completeUrl, new StringEntity(model.getData().toString()), "application/json", response);
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

	/**
	 * Executes a put request and adds basic authentication headers to the request.
	 */
	public void putWithAuthentication(Context context,
									  String url,
									  String username,
									  String password,
									  int id,
									  Model model,
									  AsyncHttpResponseHandler response) throws UnsupportedEncodingException {
		
		String completeUrl = getAbsoluteUrl(url) + id;
		completeUrl = prepareUrl(completeUrl);
		Header[] headers = {createBasicAuthenticationHeader(username, password)};
		StringEntity modelEntity = new StringEntity(model.getData().toString());
		Log.d("REST", model.getData().toString());
		client.put(context, completeUrl, headers, modelEntity, "application/json", response);
	}
	
	public void putWithAuthentication(Context context,
			  String url,
			  String username,
			  String password,
			  Model model,
			  AsyncHttpResponseHandler response) throws UnsupportedEncodingException {

		String completeUrl = getAbsoluteUrl(url);
		completeUrl = prepareUrl(completeUrl);
		Header[] headers = {createBasicAuthenticationHeader(username, password)};
		StringEntity modelEntity = new StringEntity(model.getData().toString());
		
		client.put(context, completeUrl, headers, modelEntity, "application/json", response);
	}

	/**
	 * Executes a post request and adds basic authentication headers to the request.
	 */
	public void postWithAuthentication(Context context,
									   String url,
									   String username, 
									   String password,
									   Model model, 
									   AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {
		
		String completeUrl = getAbsoluteUrl(url);
		completeUrl = prepareUrl(completeUrl);
		Header[] headers = {createBasicAuthenticationHeader(username, password)};
		StringEntity modelEntity = new StringEntity(model.getData().toString());
		
		Log.d("REST", model.getData().toString());
		client.post(context, completeUrl, headers, modelEntity, "application/json", 
				responseHandler);
	}	
	
	public void postWithAuthentication(Context context,
			   String url,
			   String username, 
			   String password,
			   AsyncHttpResponseHandler responseHandler) throws UnsupportedEncodingException {

		String completeUrl = getAbsoluteUrl(url);
		completeUrl = prepareUrl(completeUrl);
		Header[] headers = {createBasicAuthenticationHeader(username, password)};
		StringEntity modelEntity = new StringEntity("");
		client.post(context, completeUrl, headers, modelEntity, "application/json", 
		responseHandler);
}	
	
	
	// This overloading of the postWithAuthentication method takes a bitmap, and posts
	// it as an PNG HTTP Entity.
	public void postWithAuthentication(Context context, String url, Bitmap bm, String username,
			String password, JsonHttpResponseHandler responseHandler, int timeout) {
		
		String completeUrl = getAbsoluteUrl(url);
		completeUrl = prepareUrl(completeUrl);
		
		// Content type also needs to be pinned down in the Bitmap.compress
		// call, which is why I haven't exposed it as a parameter.
		String contentType = "image/png";
				
		// We need to coerce the bitmap into a ByteArrayEntity so that we can post it.
		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
		bm.compress(CompressFormat.PNG, 0, bos); 
		byte[] bitmapdata = bos.toByteArray();
		ByteArrayEntity bae = new ByteArrayEntity(bitmapdata);
		
		// This client needs authentication headers because there is no post method that takes both
		// an entity and headers.  I assert that we don't get anything for free anyway by having one
		// global AsyncHttpClient object because instantiation of that class is so easy.
		AsyncHttpClient authenticatedClient = createAutheniticatedHttpClient(username, password);
		
		authenticatedClient.setTimeout(timeout);
		authenticatedClient.post(context, completeUrl, bae, contentType, responseHandler);
	}
	
	public void delete(String url, AsyncHttpResponseHandler responseHandler) {
		client.delete(getAbsoluteUrl(url), responseHandler);
	}

	/**
	 * Executes a delete request and adds basic authentication headers to the request.
	 */
	public void deleteWithAuthentication(Context context, String url, String username, 
			String password, AsyncHttpResponseHandler responseHandler) {
		String completeUrl = getAbsoluteUrl(url);
		completeUrl = prepareUrl(completeUrl);
		Header[] headers = {createBasicAuthenticationHeader(username, password)};
		client.delete(context, completeUrl, headers, responseHandler);
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
	
	private String prepareUrl(String url) {
		// Not all methods of AsynchHttpClients take a requestParams.
		// Sometimes we will need to put the api key and other data
		// directly in the URL.
		return url + "?apikey=" + getApiKey(); 
	}
	
	private String getBaseUrl() {
		String baseUrl = prefs.getString("base_url", "");
		return baseUrl;
	}
	
	private String getApiKey() {
		String apiKey = prefs.getString("api_key", "");
		return apiKey;
	}
	
	private String getAbsoluteUrl(String relativeUrl) {
		Log.d(App.LOG_TAG, baseUrl + relativeUrl);
		return baseUrl + relativeUrl;
	}
	
	private Header createBasicAuthenticationHeader(String username, String password) {
		String credentials = String.format("%s:%s", username, password);
		String encoded = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
		return new BasicHeader("Authorization", String.format("%s %s", "Basic", encoded));
	}
	
	private AsyncHttpClient createHttpClient() {
		AsyncHttpClient client = new AsyncHttpClient();
		client.addHeader("platform-ver-build", appVersion);
		return client;
	}
	
	private AsyncHttpClient createAutheniticatedHttpClient(String username, String password) {
		AsyncHttpClient client = createHttpClient();
		Header header = createBasicAuthenticationHeader(username, password);
		client.addHeader(header.getName(), header.getValue());
		return client;
	}
	
	private String getAppVersion() {
		return prefs.getString("platform_ver_build", "");
	}
}
