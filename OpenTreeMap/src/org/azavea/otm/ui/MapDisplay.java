package org.azavea.otm.ui;

import org.azavea.map.OTMMapView;
import org.azavea.map.WMSTileRaster;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Tree;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MyLocationOverlay;
import com.loopj.android.http.BinaryHttpResponseHandler;


public class MapDisplay extends MapActivity {

	final private int FILTER_INTENT = 1;
	final private int INFO_INTENT = 2;
	
	private MyLocationOverlay myLocationOverlay;
	private OTMMapView mapView;
	private WMSTileRaster surfaceView;
	private int zoomLevel;
	private RelativeLayout plotPopup;
	private Plot currentPlot; // The Plot we're currently showing a popup for, if any
	
	// Pop-up view items
	TextView plotSpeciesView;
	TextView plotAddressView;
	TextView plotDiameterView;
	TextView plotUpdatedByView;
	ImageView plotImageView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zoomLevel = 14;
        setContentView(R.layout.activity_map_display);

        // Get a MapView and enable zoom controls
        mapView = (OTMMapView) findViewById(R.id.mapview1);
        mapView.setBuiltInZoomControls(true);
        
        // Get tree-overlay and configure
        surfaceView = (WMSTileRaster)findViewById(R.id.tileraster);
        surfaceView.setZOrderOnTop(true);
        SurfaceHolder sh = surfaceView.getHolder();
        sh.setFormat(PixelFormat.TRANSPARENT);
        
		plotPopup = (RelativeLayout) findViewById(R.id.plotPopup);
        
        surfaceView.setMapView(getWindowManager(), this);
        
        MapController mapController = mapView.getController();
        GeoPoint p = new GeoPoint((int)(39.952622*1E6), (int)(-75.165708*1E6));
        mapController.setCenter(p);
        mapController.setZoom(zoomLevel);
        
        // Force the MapView to redraw
        mapView.invalidate();
        
        setPopupViews();
    }
    
    public OTMMapView getMapView() {
    	return this.mapView;
    }
    
    @Override
    protected void onStart() {
    	super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_map_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_filter:
            	Intent filter = new Intent(this, FilterDisplay.class);
            	startActivityForResult(filter, FILTER_INTENT);
            	break;
        }
        return true;
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	surfaceView.setMapView(getWindowManager(), this);
    	this.mapView.invalidate();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	//myLocationOverlay.disableMyLocation();
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
    public boolean isRouteDisplayed() {
    	return false;
    }

    private void setPopupViews() {
    	plotSpeciesView = (TextView) findViewById(R.id.plotSpecies);
    	plotAddressView = (TextView) findViewById(R.id.plotAddress);
    	plotDiameterView = (TextView) findViewById(R.id.plotDiameter);
    	plotUpdatedByView = (TextView) findViewById(R.id.plotUpdatedBy);
    	plotImageView = (ImageView) findViewById(R.id.plotImage);
    }
    
	public void showPopup(Plot plot) {

		//set default text
		plotDiameterView.setText(getString(R.string.dbh_missing));
		plotSpeciesView.setText(getString(R.string.species_missing));
		plotAddressView.setText(getString(R.string.address_missing));
		plotImageView.setImageResource(R.drawable.ic_action_search);
		
		try {
	        GeoPoint p = new GeoPoint((int)(plot.getGeometry().getLatE6()), (int)(plot.getGeometry().getLonE6()));
	        mapView.getController().stopAnimation(false);
	        mapView.getController().animateTo(p);
			plotUpdatedByView.setText(plot.getLastUpdatedBy());
	        if (plot.getAddress().length() != 0) {
	        	plotAddressView.setText(plot.getAddress());
	        }
			Tree tree = plot.getTree();
			if (tree != null) {
			
				String speciesName;
				try {
					speciesName = tree.getSpeciesName();
				} catch (JSONException e) {
					speciesName = "No species name";
				}
				plotSpeciesView.setText(speciesName);
			
				if (tree.getDbh() != 0) {
					plotDiameterView.setText(String.valueOf(tree.getDbh()) + " " + getString(R.string.dbh_units));
				} 
				
				showImage(plot);
			
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		currentPlot = plot;
		plotPopup.setVisibility(View.VISIBLE);
	}

	public void hidePopup() {
		RelativeLayout plotPopup = (RelativeLayout) findViewById(R.id.plotPopup);
		plotPopup.setVisibility(View.INVISIBLE);
		currentPlot = null;
	}

	@Override
	public void onBackPressed() {
		hidePopup();
	}
	
	private void showImage(Plot plot) throws JSONException {
		plot.getTreePhoto(new BinaryHttpResponseHandler(Plot.IMAGE_TYPES) {
			@Override
			public void onSuccess(byte[] imageData) {
				Bitmap scaledImage = Plot.createTreeThumbnail(imageData);
				ImageView plotImage = (ImageView) findViewById(R.id.plotImage);
				plotImage.setImageBitmap(scaledImage);
			}
			
			@Override
			public void onFailure(Throwable e, byte[] imageData) {
				// Log the error, but not important enough to bother the user
				Log.e(App.LOG_TAG, "Could not retreive tree image", e);
			}
		});
	}
	
	// onClick handler for tree-details pop-up touch event
	public void showFullTreeInfo(View view) {
		// Show TreeInfoDisplay with current plot
		Intent viewPlot = new Intent(MapDisplay.this, TreeInfoDisplay.class);
		viewPlot.putExtra("plot", currentPlot.getData().toString());
		
		if (App.getLoginManager().isLoggedIn()) {
			viewPlot.putExtra("user", App.getLoginManager().loggedInUser.getData().toString());
		}
		startActivityForResult(viewPlot, INFO_INTENT);
	}
	
    // onClick handler for "My Location" button
    public void showMyLocation(View view) {
    	OTMMapView mapView = (OTMMapView) findViewById(R.id.mapview1);
    	MapController mc = mapView.getController();
    	mc.setCenter(myLocationOverlay.getMyLocation());
    }
    
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
	  super.onActivityResult(requestCode, resultCode, data); 
	  switch(requestCode) { 
	  	case FILTER_INTENT : 
	  		if (resultCode == Activity.RESULT_OK) { 
	  			Toast.makeText(this, App.getFilterManager().getActiveFiltersAsQueryString(),
	  					Toast.LENGTH_LONG).show();
	  		} 
	  		break; 
	  	case INFO_INTENT:
	  		if (resultCode == TreeDisplay.RESULT_PLOT_EDITED) {

	  			try {
	  				// The plot was updated, so update the pop-up with any new data
		  			Plot updatedPlot = new Plot();
		  			String plotJSON = data.getExtras().getString("plot");
					updatedPlot.setData(new JSONObject(plotJSON));
					showPopup(updatedPlot);
					
				} catch (JSONException e) {
					Log.e(App.LOG_TAG, "Unable to deserialze updated plot for map popup", e);
					hidePopup();
				}
	  		}
	  		break;
	  } 
	}
}
