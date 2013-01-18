package org.azavea.otm.ui;

//import org.azavea.map.OTMMapView;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.azavea.map.TileProviderFactory;
import org.azavea.map.WMSTileProvider;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.User;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class TreeDisplay extends MapActivity{
	protected LatLng plotLocation;
	protected Plot plot;
	public static int RESULT_PLOT_DELETED =  Activity.RESULT_FIRST_USER + 1;
	public static int RESULT_PLOT_EDITED = Activity.RESULT_FIRST_USER + 2;
	protected static final int DEFAULT_TREE_ZOOM_LEVEL = 18;
	protected GoogleMap mMap;
	protected Marker  plotMarker;
	protected int mapFragmentId;
	
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
			plot = new Plot();
			plot.setData(new JSONObject(getIntent().getStringExtra("plot")));
			plotLocation = getPlotLocation(plot);
		} catch (JSONException e) {
			Toast.makeText(this, "Could not retrieve Tree information", 
					Toast.LENGTH_SHORT).show();
			Log.e(App.LOG_TAG, "Failed to create tree view", e);
		}
       
    }
    
    protected LatLng getPlotLocation(Plot plot) {
    	try {
			double lon = plot.getGeometry().getLon();
			double lat = plot.getGeometry().getLat();
			return new LatLng(lat, lon);
    	} catch (Exception e) {
    		return null;
    	}
    }
    
	protected void showPositionOnMap() {
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(plotLocation, DEFAULT_TREE_ZOOM_LEVEL));
		if (plotMarker != null) {
			plotMarker.remove();
		}
		plotMarker = mMap.addMarker(new MarkerOptions().position(plotLocation).title(""));
	}
	
	 protected void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            // we have to try for 2 different fragment id's, using the reasonable
        	// assumption that the base classes are going to be instantiated one at a time.
        	Log.d("VIN", String.format("%d", mapFragmentId));
        	FragmentManager fragmentManager = getSupportFragmentManager();
            SupportMapFragment fragment = (SupportMapFragment) fragmentManager.findFragmentById(mapFragmentId);
            mMap = fragment.getMap();
        	if (mMap != null) {
                setUpMap();
            } else {
            	Log.e("VIN", "map was null.");
            }
        }
	 }
	 
	 private void setUpMap() {
		TileProvider tileProvider = TileProviderFactory.getTileProvider("otm");
		mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
		UiSettings mUiSettings = mMap.getUiSettings();
		mUiSettings.setZoomControlsEnabled(false);
		mUiSettings.setScrollGesturesEnabled(false);
		mUiSettings.setZoomGesturesEnabled(false);
		mUiSettings.setTiltGesturesEnabled(false);
		mUiSettings.setRotateGesturesEnabled(false);
	 }
	 
	 
	protected void setText(int resourceId, String text) {
		// Only set the text if it exists, letting the layout define default text
		if (text != null &&  !"".equals(text)) {
			((TextView)findViewById(resourceId)).setText(text);
		}
	}

}

