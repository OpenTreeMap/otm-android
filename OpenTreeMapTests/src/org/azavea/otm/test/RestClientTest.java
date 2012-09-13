package org.azavea.otm.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.azavea.otm.rest.RestClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.loopj.android.http.*;

import junit.framework.Assert;
import junit.framework.TestCase;

import java.lang.Object;

import static org.mockito.Mockito.*;

public class RestClientTest extends TestCase {
	private RestClient rc;
	private String urlBase;
	
	public void setUp() {
		rc = new RestClient();
		urlBase = "http://207.245.89.246/v1.2/api/v0.1";
	}
	
	public void tearDown() {
		rc = null;
		urlBase = "";
	}
	
	// Does .get() correctly process its parameters
	// and route them to the underlying AsyncHttpClient object?
	public void testGet() throws ClassNotFoundException {
		// All we care about as far as these are concerned is that
		// the objects we pass to the RestClient object and passed to the
		// underlying AsyncHttpClient object
		RequestParams params = new RequestParams();
		JsonHttpResponseHandler jsonClient = new JsonHttpResponseHandler();
		
		// Create a mock async client that expects a complete URL
		AsyncHttpClient mockClient = mock(AsyncHttpClient.class);

		// Inject mock
		rc.setAsyncClient(mockClient);

		// Make the call
		rc.get("/get", params, jsonClient);
		
		// See if mockClient.get() got called as expected
		verify(mockClient).get(urlBase + "/get", params, jsonClient);
	}
	
	// Does .post() correctly process its parameters
	// and route them to the underlying AsyncHttpClient object?
//	@UsesMocks(AsyncHttpClient.class)
	public void testPost() throws ClassNotFoundException {
		// All we care about as far as these are concerned is that
		// the objects we pass to the RestClient object and passed to the
		// underlying AsyncHttpClient object
		RequestParams params = new RequestParams();
		JsonHttpResponseHandler jsonClient = new JsonHttpResponseHandler();
		
		// Create a mock async client that expects a complete URL
		AsyncHttpClient mockClient = mock(AsyncHttpClient.class);

		// Inject mock
		rc.setAsyncClient(mockClient);

		// Make the call
		rc.post("/put", params, jsonClient);
		
		// See if mockClient.get() got called as expected
		verify(mockClient).post(urlBase + "/put",  params, jsonClient);
	}
	
	// Does .delete() correctly process its parameters
	// and route them to the underlying AsyncHttpClient object?
	public void testDelete() throws ClassNotFoundException {
		// All we care about as far as these are concerned is that
		// the objects we pass to the RestClient object and passed to the
		// underlying AsyncHttpClient object
		JsonHttpResponseHandler jsonClient = new JsonHttpResponseHandler();
		
		// Create a mock async client that expects a complete URL
		AsyncHttpClient mockClient = mock(AsyncHttpClient.class);

		// Inject mock
		rc.setAsyncClient(mockClient);

		// Make the call
		rc.delete("/delete", jsonClient);
		
		// See if mockClient.delete() got called as expected
		verify(mockClient).delete(urlBase + "/delete", jsonClient);
	}
	
	// Is a null parameter-value for RequestParams
	// replaced with a RequestParams object?
	public void testNullParams() throws ClassNotFoundException {
		AsyncHttpClient mockClient = mock(AsyncHttpClient.class);
		
		JsonHttpResponseHandler jsonClient = new JsonHttpResponseHandler();
		
		rc.setAsyncClient(mockClient);
		
		rc.get("/get", null, jsonClient);
		
		verify(mockClient).get(eq(urlBase + "/get"), isA(RequestParams.class), eq(jsonClient));
	}
}