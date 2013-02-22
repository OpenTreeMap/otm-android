package org.azavea.otm.ui;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.azavea.otm.App;
import org.azavea.otm.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Tree;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.BinaryHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TreeInfoDisplay extends TreeDisplay{
	public final static int EDIT_REQUEST = 1;
	ImageView plotImage;
	
    public void onCreate(Bundle savedInstanceState) {
    	mapFragmentId = R.id.vignette_map_view_mode;
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.plot_view_activity);
    	setUpMapIfNeeded();
    	plotImage = (ImageView) findViewById(R.id.plot_photo);
    	loadPlotInfo();
    }
    
    
    private void loadPlotInfo()  {
    	
    	try {
    		LinearLayout fieldList = (LinearLayout)findViewById(R.id.field_list);
            fieldList.removeAllViewsInLayout();
            LayoutInflater layout = ((Activity)this).getLayoutInflater();
    
    		Log.d(App.LOG_TAG, "Setting header values");
    		setHeaderValues(plot);
    		showPositionOnMap();
    		for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
    			View fieldGroup = group.renderForDisplay(layout, plot, TreeInfoDisplay.this);
    			if (fieldGroup != null) {
    				fieldList.addView(fieldGroup);
    			}
    		}
    		
    		showImage(plot);
    	}
    	catch (Exception e) {
    		Log.e(App.LOG_TAG, "Unable to render tree view", e);
    		Toast.makeText(App.getInstance(), 
    				"Unable to render view for display", Toast.LENGTH_SHORT).show();
    		finish();
    	}
        
    }
   
	private void setHeaderValues(Plot plot) {
		try {
			setText(R.id.address, plot.getAddress());
			
			Tree tree = plot.getTree();
			String defaultText = getResources().getString(R.string.species_missing);
			if (tree != null) {
				setText(R.id.species, tree.getSpeciesName(defaultText));
			} else {
				setText(R.id.species, defaultText);
			}
			try {
				DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				DateFormat outputFormat = new SimpleDateFormat(getString(R.string.date_time_format));
				Date updated = inputFormat.parse(plot.getLastUpdated());
				setText(R.id.updated_on, "Updated " + outputFormat.format(updated));	
			} catch (Exception e) {
				e.printStackTrace();
			}
			setText(R.id.updated_by, "By " + plot.getLastUpdatedBy());
		} catch (JSONException e) {
			Toast.makeText(this, "Could not access plot information for display", 
					Toast.LENGTH_SHORT).show();
			Log.e(App.LOG_TAG, "Failed to create tree view", e);
		}
		
	}
	
	private void showImage(Plot plot) throws JSONException {
		// Default if there is no image returned
		plotImage.setImageResource(R.drawable.ic_action_search);

		plot.getTreePhoto(new BinaryHttpResponseHandler(Plot.IMAGE_TYPES) {
			@Override
			public void onSuccess(byte[] imageData) {
				Bitmap scaledImage = Plot.createTreeThumbnail(imageData);
				plotImage.setImageBitmap(scaledImage);
			}
			
			@Override
			public void onFailure(Throwable e, byte[] imageData) {
				// Log the error, but not important enough to bother the user
				Log.e(App.LOG_TAG, "Could not retreive tree image", e);
			}
		});
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_plot_edit_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.edit_plot) {
			if (App.getLoginManager().isLoggedIn()) {
				Intent editPlot = new Intent(this, TreeEditDisplay.class);
				editPlot.putExtra("plot", plot.getData().toString());
				startActivityForResult(editPlot, EDIT_REQUEST);	
			} else {
				// TODO: This should redirect to login page
				startActivity(new Intent(TreeInfoDisplay.this, LoginActivity.class));
			}
		}
        return true;
    }
    
    @Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	  	case EDIT_REQUEST:  
	  		
	  		if (resultCode == Activity.RESULT_OK) { 
	  			// The tree/plot has been updated, or the tree has been deleted
	  			String plotJSON = data.getStringExtra("plot");
	  			if (plotJSON != null) {
	  				
	  				// The plot has been edited, reload the info page
	  				plot = new Plot();
	  				try {
						plot.setData(new JSONObject(plotJSON));
						loadPlotInfo();
						
						// Pass along the updated plot
						Intent updatedPlot = new Intent();
						updatedPlot.putExtra("plot", plotJSON);
						setResult(TreeDisplay.RESULT_PLOT_EDITED, updatedPlot);
						
					} catch (JSONException e) {
						Log.e(App.LOG_TAG, "Unable to load edited plot");
						finish();
					}
	  			}
	  			
  			} else if (resultCode == RESULT_PLOT_DELETED){
  				// If the plot is deleted, finish back to the caller
  				setResult(RESULT_PLOT_DELETED);
  				finish();
  				
	  		} else if (resultCode == Activity.RESULT_CANCELED) {
	  			// Do nothing?
	  		}
	  		break; 
	     
	  } 
	}    
}
