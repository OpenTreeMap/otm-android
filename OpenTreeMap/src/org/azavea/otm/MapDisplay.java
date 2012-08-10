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
        surfaceView.setMapView(mapView);

//        // Create overlay showing user's location and add to MapView
//        myLocationOverlay = new MyLocationOverlay(this, mapView);
//        
//        List<Overlay> overlays = mapView.getOverlays();
//        overlays.clear();
//        overlays.add(myLocationOverlay);
//        
//        // Force the MapView to redraw
//        mapView.invalidate();
        
        // For example purposes: manually set location and zoom
/*        GeoPoint myLoc = myLocationOverlay.getMyLocation();
        MapController mc = mapView.getController();
        String coordinates[] = {"30", "71"};
        double lat = Double.parseDouble(coordinates[0]);
        double lng = Double.parseDouble(coordinates[1]);

        GeoPoint p = new GeoPoint(
        (int) (lat * 1E6),
        (int) (lng * 1E6));

        mc.animateTo(myLoc);
        mc.setZoom(7);
        mapView.invalidate(); */

        MapController mapController = mapView.getController();
        GeoPoint p = new GeoPoint((int)(39.952622*1E6), (int)(-75.165708*1E6));
        //GeoPoint p = new GeoPoint((int)(75.1642*1E6), (int)(39.9522*1E6));
        mapController.setCenter(p);
        mapController.setZoom(14);
        
//        mapView.setOnTouchListener(new OnTouchListener() {
//        	@Override
//        	public boolean onTouch(View v, MotionEvent event) {
//        		// TODO Auto-generated method stub
//                Projection proj = mapView.getProjection();
//                GeoPoint projectedTopLeft = proj.fromPixels(mapView.getLeft(), mapView.getTop());
//                GeoPoint projectedBottomRight = proj.fromPixels(mapView.getLeft() + 480, mapView.getTop() - 800);
//                new WMSAsyncTask(mapView, 0, 0).execute(projectedTopLeft.getLongitudeE6()/1E6, projectedTopLeft.getLatitudeE6()/1E6,
//                		projectedBottomRight.getLongitudeE6()/1E6, projectedBottomRight.getLatitudeE6()/1E6);
//                return true;
//        	}
//        });
        
//        Projection proj = mapView.getProjection();
//        GeoPoint projectedTopLeft = proj.fromPixels(mapView.getLeft(), mapView.getTop());
//        GeoPoint projectedBottomRight = proj.fromPixels(mapView.getLeft() + 480, mapView.getTop() - 800);
//        new WMSAsyncTask(mapView, 0, 0).execute(projectedTopLeft.getLongitudeE6()/1E6, projectedTopLeft.getLatitudeE6()/1E6,
//        		projectedBottomRight.getLongitudeE6()/1E6, projectedBottomRight.getLatitudeE6()/1E6);
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

//    @Override
//    public void onContentChanged() {
//    	// TODO Auto-generated method stub
//    	super.onContentChanged();
//        Projection proj = mapView.getProjection();
//        GeoPoint projectedTopLeft = proj.fromPixels(mapView.getLeft(), mapView.getTop());
//        GeoPoint projectedBottomRight = proj.fromPixels(mapView.getLeft() + 480, mapView.getTop() - 800);
//        new WMSAsyncTask(mapView, 0, 0).execute(projectedTopLeft.getLongitudeE6()/1E6, projectedTopLeft.getLatitudeE6()/1E6,
//        		projectedBottomRight.getLongitudeE6()/1E6, projectedBottomRight.getLatitudeE6()/1E6);    	
//    }
    
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
