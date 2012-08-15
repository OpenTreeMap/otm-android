package org.azavea.otm;

import java.util.List;

import org.azavea.map.WMSAsyncTask;
import org.azavea.map.WMSOverlay;
import org.azavea.map.WMSTileRaster;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;


public class MapDisplay extends MapActivity {

	private MyLocationOverlay myLocationOverlay;
	private MapView mapView;
	private WMSTileRaster surfaceView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_display);

        // Get a MapView and enable zoom controls
        mapView = (MapView) findViewById(R.id.mapview1);
        mapView.setBuiltInZoomControls(true);
        
        surfaceView = (WMSTileRaster) findViewById(R.id.tileraster);
        surfaceView.setZOrderOnTop(true);
        SurfaceHolder sh = surfaceView.getHolder();
        sh.setFormat(PixelFormat.TRANSPARENT);
        //surfaceView.setMapView(mapView);

        MapController mapController = mapView.getController();
        GeoPoint p = new GeoPoint((int)(39.952622*1E6), (int)(-75.165708*1E6));
        mapController.setCenter(p);
        mapController.setZoom(14);
    }
    
    @Override
    protected void onStart() {
    	// TODO Auto-generated method stub
    	super.onStart();
    	surfaceView.setMapView(getWindowManager(), mapView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_map_display, menu);
        return true;
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	//myLocationOverlay.enableMyLocation();
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
    	MapView mapView = (MapView) findViewById(R.id.mapview1);
    	Log.d("MapDisplay", "" + mapView.getLeft());
    	Log.d("MapDisplay", "" + mapView.getRight());
    	MapController mc = mapView.getController();
    	mc.setCenter(myLocationOverlay.getMyLocation());
    	mc.setZoom(7);
    }
}
