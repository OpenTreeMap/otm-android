package org.azavea.otm.ui;

import org.azavea.map.OTMMapView;
import org.azavea.map.WMSTileRaster;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Tree;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.opengl.Visibility;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;


public class MapDisplay extends MapActivity {

	final private int FILTER_INTENT = 1;
	
	private MyLocationOverlay myLocationOverlay;
	private OTMMapView mapView;
	private WMSTileRaster surfaceView;
	private int zoomLevel;
	
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
        
        surfaceView.setMapView(getWindowManager(), this);
        
        MapController mapController = mapView.getController();
        GeoPoint p = new GeoPoint((int)(39.952622*1E6), (int)(-75.165708*1E6));
        mapController.setCenter(p);
        mapController.setZoom(14);
        
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
            case R.id.temp_view:
            	RequestGenerator rg = new RequestGenerator();
			try {
				rg.getPlot(54248, new RestHandler<Plot>(new Plot()) {
					@Override
					public void onFailure(Throwable e, String message){
						Log.e(App.LOG_TAG, "Bad Plot Request" , e);
					}
					
            		@Override
            		public void dataReceived(Plot plot) {
            			Intent viewPlot = new Intent(MapDisplay.this, TreeInfoDisplay.class);
            			viewPlot.putExtra("plot", plot.getData().toString());
            			startActivity(viewPlot);
            		}
            	});
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
    public boolean isRouteDisplayed() {
    	return false;
    }

	public void showPopup(Plot plot) {
		RelativeLayout plotPopup = (RelativeLayout) findViewById(R.id.plotPopup);
		TextView plotSpecies = (TextView) findViewById(R.id.plotSpecies);
		TextView plotAddress = (TextView) findViewById(R.id.plotAddress);
		TextView plotDiameter = (TextView) findViewById(R.id.plotDiameter);
		TextView plotUpdatedBy = (TextView) findViewById(R.id.plotUpdatedBy);
		//set default text
		plotDiameter.setText(R.string.dbh_missing);
		plotSpecies.setText(R.string.species_missing);
		plotAddress.setText(R.string.address_missing);
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
					plotDiameter.setText(String.valueOf(tree.getDbh()) + " " + R.string.dbh_units);
				} 
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		plotPopup.setVisibility(View.VISIBLE);
	}

	public void hidePopup() {
		RelativeLayout plotPopup = (RelativeLayout) findViewById(R.id.plotPopup);
		plotPopup.setVisibility(View.INVISIBLE);
		
	}
    // onClick handler for "My Location" button
    public void showMyLocation(View view) {
    	surfaceView.forceReInit();
    	MapView mapView = (MapView) findViewById(R.id.mapview1);
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
