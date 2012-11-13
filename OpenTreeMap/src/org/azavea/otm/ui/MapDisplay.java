package org.azavea.otm.ui;

import java.util.ArrayList;

import org.azavea.map.OTMMapView;
import org.azavea.map.WMSTileRaster;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Tree;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Bundle;
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
	
	private MyLocationOverlay myLocationOverlay;
	private OTMMapView mapView;
	private WMSTileRaster surfaceView;
	private int zoomLevel;
	private RelativeLayout plotPopup;
	private Plot currentPlot; // The Plot we're currently showing a popup for, if any
	
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

	public void showPopup(Plot plot) {
		TextView plotSpecies = (TextView) findViewById(R.id.plotSpecies);
		TextView plotAddress = (TextView) findViewById(R.id.plotAddress);
		TextView plotDiameter = (TextView) findViewById(R.id.plotDiameter);
		TextView plotUpdatedBy = (TextView) findViewById(R.id.plotUpdatedBy);
		//set default text
		plotDiameter.setText(getString(R.string.dbh_missing));
		plotSpecies.setText(getString(R.string.species_missing));
		plotAddress.setText(getString(R.string.address_missing));
		try {
	        GeoPoint p = new GeoPoint((int)(plot.getGeometry().getLatE6()), (int)(plot.getGeometry().getLonE6()));
	        mapView.getController().stopAnimation(false);
	        mapView.getController().animateTo(p);
			plotUpdatedBy.setText(plot.getLastUpdatedBy());
	        if (plot.getAddress().length() != 0) {
	        	plotAddress.setText(plot.getAddress());
	        }
			Tree tree = plot.getTree();
			if (tree != null) {
				plotSpecies.setText(tree.getSpeciesName());
				if (tree.getDbh() != 0) {
					plotDiameter.setText(String.valueOf(tree.getDbh()) + " " + getString(R.string.dbh_units));
				} 
				ArrayList<Integer> imageIds = tree.getImageIdList();

				if (imageIds != null && imageIds.size() > 0) {
					showImage(imageIds.get(0).intValue(), plot.getId());
				}
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
	
	public void showImage(int imageId, int plotId) {
		RequestGenerator rg = new RequestGenerator();
		String[] allowedTypes = new String[] { "image/jpeg", "image/png", "image/gif" };
		rg.getImage(plotId, imageId, new BinaryHttpResponseHandler(allowedTypes) {
			@Override
			public void onSuccess(byte[] imageData) {
				Bitmap image = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
				Bitmap scaledImage = Bitmap.createScaledBitmap(image, 80, 80, true);
				ImageView plotImage = (ImageView) findViewById(R.id.plotImage);
				plotImage.setImageBitmap(scaledImage);
			}
			
			@Override
			public void onFailure(Throwable e, byte[] imageData) {
				e.printStackTrace();
			}
		});
	}
	
	// onClick handler for tree-details popup touch-event
	public void showFullTreeInfo(View view) {
		// Show TreeInfoDisplay with current plot
		Intent viewPlot = new Intent(MapDisplay.this, TreeInfoDisplay.class);
		viewPlot.putExtra("plot", currentPlot.getData().toString());
		
		if (App.getLoginManager().isLoggedIn()) {
			viewPlot.putExtra("user", App.getLoginManager().loggedInUser.getData().toString());
		}
		startActivity(viewPlot);
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
	  	case (FILTER_INTENT) : { 
	  		if (resultCode == Activity.RESULT_OK) { 
	  			Toast.makeText(this, App.getFilterManager().getActiveFiltersAsQueryString(),
	  					Toast.LENGTH_LONG).show();
	  		} 
	  		break; 
	    } 
	  } 
	}
}
