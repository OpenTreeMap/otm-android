package org.azavea.otm;

import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Version;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

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
    				output.setText(response.getAddressStreet() + "\n" + response.getTree().getSpeciesName());
    				RequestGeneratorUi.this.plot = response;
    			} catch (JSONException e) {
    				output.setText("Exception: " + e.getMessage());
    			}
    		}
    	});
    }
    
    public void updatePlot(View view) throws JSONException {
    	plot.setAddressStreet("210 Sweet St Ne");
    	try {
    		rg.updatePlot(this, 329, plot, new RestHandler<Plot>(new Plot()) {
    			@Override
    			public void dataReceived(Plot plot) {
    				try {
    					output.setText(plot.getAddressStreet());
    				} catch (Exception e) {
    					output.setText(e.getMessage());
    				}
    			}
    		});
    	} catch (Exception e) {
    		output.setText(e.getMessage());
    	}
    }
}
