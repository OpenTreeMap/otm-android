package org.azavea.otm;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;


public class MapDisplay extends MapActivity {

	private MyLocationOverlay myLocationOverlay;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_display);

        // Get a MapView and enable zoom controls
        MapView mapView = (MapView) findViewById(R.id.mapview1);
        mapView.setBuiltInZoomControls(true);
        
        // Create overlay showing user's location and add to MapView
        myLocationOverlay = new MyLocationOverlay(this, mapView);
    	
        List<Overlay> overlays = mapView.getOverlays();
        overlays.clear();
        overlays.add(myLocationOverlay);        
        
        // Force the MapView to redraw
        mapView.invalidate();
        
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_map_display, menu);
        return true;
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	myLocationOverlay.enableMyLocation();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	myLocationOverlay.disableMyLocation();
    }
    
    @Override
    public boolean isRouteDisplayed() {
    	return false;
    }
    
    // onClick handler for "My Location" button
    public void showMyLocation(View view) {
    	MapView mapView = (MapView) findViewById(R.id.mapview1);
    	MapController mc = mapView.getController();
    	mc.setCenter(myLocationOverlay.getMyLocation());
    	mc.setZoom(7);
    }
}
