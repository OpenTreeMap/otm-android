package org.azavea.otm;

import org.azavea.otm.data.User;
import org.azavea.otm.rest.RestClient;
import org.json.JSONException;
import org.json.JSONObject;

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
	private LoginManager loginManager = App.getLoginManager();
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void showMap(View view) {
    	// Create intent for map-view activity and switch
    	Intent intent = new Intent(view.getContext(), MapDisplay.class);
    	startActivity(intent);    			
    }
    
    public void testAsync(View view) {
    	RestClient rc = new RestClient();
    	rc.get("/get", new RequestParams(), new JsonHttpResponseHandler() {
    		@Override
    		public void onSuccess(JSONObject resp) {
    			Log.d("Anything", resp.toString());
    		}
    	});
    }
    
    public void testAuth(View view) {
    	RestClient rc = new RestClient();
    	User user = loginManager.loggedInUser;
    	try {
			rc.getWithAuthentication(this, "/login", user.getUsername(), 
					user.password, new RequestParams(), new JsonHttpResponseHandler() {
				@Override
				public void onSuccess(JSONObject resp) {
					Log.d("Anything", resp.toString());
				}
				
				@Override
				public void onFailure(Throwable e) {
					Log.e("Anything", "auth", e);
				}
			});
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void showRequestGeneratorUi(View view) {
    	Intent intent = new Intent(view.getContext(), RequestGeneratorUi.class);
    	startActivity(intent);
    }
}
