package org.azavea.otm;

import org.azavea.otm.data.Plot;
import org.azavea.otm.data.User;
import org.azavea.otm.data.Version;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;
import org.json.JSONArray;
import org.json.JSONException;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class RequestGeneratorUi extends Activity {
	private TextView output;
	private RequestGenerator rg;
	private Plot plot;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_generator);
        output = (TextView)findViewById(R.id.response);
        rg = new RequestGenerator();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_request_generator, menu);
        return true;
    }
    
    public void showVersion(View view) {
    	rg.getVersion(new RestHandler<Version>(new Version()) {
			@Override
			public void onSuccess(String resp) {
				Log.d(App.LOG_TAG, resp);
			}
    		@Override
			public void onFailure(Throwable e, JSONArray errorResponse){
				Log.e(App.LOG_TAG, "version bad", e);
			}
    		@Override
    		public void dataReceived(Version response) {
    			try {
    				output.setText(response.getOtmVersion() + ", " + response.getApiVersion());
    			} catch (JSONException e) {
    				output.setText("Exception: " + e.getMessage());
    			}
    		}
    	});
    }
    
    public void showPlot(View view) throws JSONException {
    	rg.getPlot(329, new RestHandler<Plot>(new Plot()) {
    		@Override
    		public void dataReceived(Plot response) {
    			try {
    				output.setText(response.getWidth() + "\n" + response.getTree().getSpeciesName() + "\n");
    				RequestGeneratorUi.this.plot = response;
    			} catch (JSONException e) {
    				output.setText("Exception: " + e.getMessage());
    			}
    		}
    	});
    }
    
    public void updatePlot(View view) throws JSONException {
    	//plot.setPowerlineConflictPotential("5");
    	plot.setWidth(plot.getWidth()+1);
    	try {
    		rg.updatePlot(329, plot, new RestHandler<Plot>(new Plot()) {
    			@Override
    			public void onSuccess(String resp) {
    				Log.d(App.LOG_TAG, resp);
    			}
    		});
    	} catch (Exception e) {
    		output.setText(e.getMessage());
    	}
    }
    
    public void addUser(View view) throws JSONException {
    	EditText userName = (EditText)findViewById(R.id.userName);
    	EditText actualName = (EditText)findViewById(R.id.name);
    	EditText email = (EditText)findViewById(R.id.email);
    	EditText password = (EditText)findViewById(R.id.password);
    	
    	String[] names = actualName.getText().toString().split(" ");
    	User user = new User(userName.getText().toString(), names[0], names[1], email.getText().toString(), password.getText().toString(), "19087");
    	try {
    		rg.addUser(this, user, new RestHandler<User>(new User()) {
				@Override
				public void onFailure(Throwable e, JSONArray errorResponse){
					Log.e(App.LOG_TAG, "login bad", e);
				}
    			@Override
    			public void dataReceived(User resp) {
    				Log.d(App.LOG_TAG, "user received");
    			}
    		});
    	} catch (Exception e) {
    		output.setText(e.getMessage());
    	}
    }
}
