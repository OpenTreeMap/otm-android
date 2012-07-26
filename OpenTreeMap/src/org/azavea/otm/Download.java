package org.azavea.otm;

import org.azavea.otm.rest.RestClient;
import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;


public class Download extends Activity
{
	private RestClient client;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        client = new RestClient();
    }
    
    public void showMap(View view) {
    	// Create intent for map-view activity and switch
    	Intent intent = new Intent(view.getContext(), MapDisplay.class);
    	startActivity(intent);    			
    }
    
    public void testAsync(View view) {
    	RestClient rc = new RestClient();
    	client.get("/get", new RequestParams(), new JsonHttpResponseHandler() {
    		@Override
    		public void onSuccess(JSONObject resp) {
    			Log.d("Anything", resp.toString());
    		}
    	});
    }
}
