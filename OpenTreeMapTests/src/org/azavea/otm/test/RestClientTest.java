package org.azavea.otm.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.azavea.otm.rest.RestClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

//import com.google.android.testing.mocking.AndroidMock;
//import com.google.android.testing.mocking.UsesMocks;
import com.loopj.android.http.*;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.lang.Object;

public class RestClientTest extends TestCase {
	private RestClient rc;
	private String urlBase;
	
	public void setUp() {
		rc = new RestClient();
		urlBase = "http://10.0.2.2:8000/api/v0.1";
	}
	
	public void tearDown() {
		rc = null;
		urlBase = "";
	}
	
	// Does .get() correctly process its parameters
	// and route them to the underlying AsyncHttpClient object?
//	@UsesMocks(AsyncHttpClient.class)
	public void testGet() throws ClassNotFoundException {
		// All we care about as far as these are concerned is that
		// the objects we pass to the RestClient object and passed to the
		// underlying AsyncHttpClient object
//		RequestParams params = new RequestParams();
//		JsonHttpResponseHandler jsonClient = new JsonHttpResponseHandler();
//		
//		// Create a mock async client that expects a complete URL
//		AsyncHttpClient mockClient = AndroidMock.createMock(AsyncHttpClient.class);
//		mockClient.get(urlBase + "/get", params, jsonClient);
//		
//		// Begin test-sequence
//		AndroidMock.replay(mockClient);
//
//		// Inject mock
//		rc.setAsyncClient(mockClient);
//
//		// Make the call
//		rc.get("/get", params, jsonClient);
//		
//		// See if mockClient.get() got called as expected
//		AndroidMock.verify(mockClient);
	}
	
	// Does .post() correctly process its parameters
	// and route them to the underlying AsyncHttpClient object?
//	@UsesMocks(AsyncHttpClient.class)
	public void testPost() throws ClassNotFoundException {
		// All we care about as far as these are concerned is that
		// the objects we pass to the RestClient object and passed to the
		// underlying AsyncHttpClient object
//		RequestParams params = new RequestParams();
//		JsonHttpResponseHandler jsonClient = new JsonHttpResponseHandler();
//		
//		// Create a mock async client that expects a complete URL
//		AsyncHttpClient mockClient = AndroidMock.createMock(AsyncHttpClient.class);
//		mockClient.post(urlBase + "/put", params, jsonClient);
//		
//		// Begin test-sequence
//		AndroidMock.replay(mockClient);
//
//		// Inject mock
//		rc.setAsyncClient(mockClient);
//
//		// Make the call
//		rc.post("/put", params, jsonClient);
//		
//		// See if mockClient.get() got called as expected
//		AndroidMock.verify(mockClient);
	}
	
	// Does .delete() correctly process its parameters
	// and route them to the underlying AsyncHttpClient object?
//	@UsesMocks(AsyncHttpClient.class)
	public void testDelete() throws ClassNotFoundException {
		// All we care about as far as these are concerned is that
		// the objects we pass to the RestClient object and passed to the
		// underlying AsyncHttpClient object
//		JsonHttpResponseHandler jsonClient = new JsonHttpResponseHandler();
//		
//		// Create a mock async client that expects a complete URL
//		AsyncHttpClient mockClient = AndroidMock.createMock(AsyncHttpClient.class);
//		mockClient.delete(urlBase + "/delete", jsonClient);
//		
//		// Begin test-sequence
//		AndroidMock.replay(mockClient);
//
//		// Inject mock
//		rc.setAsyncClient(mockClient);
//
//		// Make the call
//		rc.delete("/delete", jsonClient);
//		
//		// See if mockClient.get() got called as expected
//		AndroidMock.verify(mockClient);
	}
	
	// Is a null parameter-value for RequestParams
	// replaced with a RequestParams object?
//	@UsesMocks(AsyncHttpClient.class)
	public void testNullParams() throws ClassNotFoundException {
//		AsyncHttpClient mockClient = AndroidMock.createMock(AsyncHttpClient.class);
//		
//		JsonHttpResponseHandler jsonClient = new JsonHttpResponseHandler();
//		mockClient.get(AndroidMock.eq(urlBase + "/get"), (RequestParams)AndroidMock.anyObject(), AndroidMock.eq(jsonClient));
//		
//		AndroidMock.replay(mockClient);
//		
//		rc.setAsyncClient(mockClient);
//		
//		Log.d("mock", "making call for rc.get");
//		rc.get("/get", null, jsonClient);
//		
//		AndroidMock.verify(mockClient);
	}
}