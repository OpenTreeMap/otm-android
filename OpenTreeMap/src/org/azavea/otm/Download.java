package org.azavea.otm;

import org.azavea.otm.rest.RestClient;
import org.azavea.otm.tasks.HttpRequest;
import org.json.JSONArray;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
    
    public void fetch(View view) {
    	
    	EditText url = (EditText)findViewById(R.id.edit_message);
    	String text = url.getText().toString();
    	new HttpRequest(this).execute(text);
    }
    
    public void showMap(View view) {
    	// Create intent for map-view activity and switch
    	Intent intent = new Intent(view.getContext(), MapDisplay.class);
    	startActivity(intent);    			
    }
    
    public void showResult(String result) {
        TextView textView = new TextView(this);
        textView.setTextSize(10);
        textView.setText(result);

        setContentView(textView);    	
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
    
    public void showRequestGeneratorUi(View view) {
    	Intent intent = new Intent(view.getContext(), RequestGeneratorUi.class);
    	startActivity(intent);
    }
}
