package org.azavea.otm.ui;

import org.azavea.map.WMSTileRaster;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.RestHandler;
import org.json.JSONException;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;


public class MapDisplay extends MapActivity {

	final private int FILTER_INTENT = 1;
	
	private MyLocationOverlay myLocationOverlay;
	private MapView mapView;
	private WMSTileRaster surfaceView;
	private int zoomLevel;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        zoomLevel = 14;
        setContentView(R.layout.activity_map_display);

        // Get a MapView and enable zoom controls
        mapView = (MapView) findViewById(R.id.mapview1);
        mapView.setBuiltInZoomControls(true);
        
        Log.d("MapDisplay", "Initializing surfaceView");

        // Get tree-overlay and configure
        surfaceView = (WMSTileRaster)findViewById(R.id.tileraster);
        surfaceView.setZOrderOnTop(true);
        SurfaceHolder sh = surfaceView.getHolder();
        sh.setFormat(PixelFormat.TRANSPARENT);
        
        surfaceView.setMapView(getWindowManager(), this);
        Log.d("MapDisplay", "Surface view configured");
        
        MapController mapController = mapView.getController();
        GeoPoint p = new GeoPoint((int)(39.952622*1E6), (int)(-75.165708*1E6));
        mapController.setCenter(p);
        mapController.setZoom(14);
        
        // Force the MapView to redraw
        mapView.invalidate();
    }
    
    public MapView getMapView() {
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
    	//myLocationOverlay.enableMyLocation();
    	WindowManager wm = getWindowManager();
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
    
    // onClick handler for "My Location" button
    public void showMyLocation(View view) {
//    	MapView mapView = (MapView) findViewById(R.id.mapview1);
    	MapController mc = mapView.getController();
//    	mc.setCenter(myLocationOverlay.getMyLocation());
    	zoomLevel++;
    	mc.setZoom(zoomLevel);
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
