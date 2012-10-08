package org.azavea.otm.ui;

import org.azavea.map.OTMMapView;
import org.azavea.otm.App;
import org.azavea.otm.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Tree;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TreeDisplay extends MapActivity{
	protected GeoPoint plotLocation;
	protected Plot plot;
	
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

		try {
			plot = new Plot();

			plot.setData(new JSONObject(getIntent().getStringExtra("plot")));
	
			Log.d("mjm", plot.getLastUpdatedBy());
			plotLocation = getPlotLocation(plot);
			
		} catch (JSONException e) {
			Toast.makeText(this, "Could not retrieve Tree information", 
					Toast.LENGTH_SHORT).show();
			Log.e(App.LOG_TAG, "Failed to create tree view", e);
		}
    }
    
    protected GeoPoint getPlotLocation(Plot plot) {
    	try {
			double lon = plot.getGeometry().getLonE6();
			double lat = plot.getGeometry().getLatE6();
			return new GeoPoint((int)lat, (int)lon);
    	} catch (Exception e) {
    		return null;
    	}
    }
    
	protected void showPositionOnMap() {
		OTMMapView mapView = (OTMMapView)findViewById(R.id.map_vignette);
		if (mapView != null) {
			mapView.getOverlays().add(new TreeLocationOverlay());
			mapView.getController().animateTo(plotLocation);
			mapView.getController().setZoom(16);
		}
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private class TreeLocationOverlay extends com.google.android.maps.Overlay {
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);

			if (!shadow) {
				
				Point point = new Point();
				mapView.getProjection().toPixels(plotLocation, point);
				
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_forest2);
				int x = point.x - bmp.getWidth() / 2;
				int y = point.y - bmp.getHeight();
				
				canvas.drawBitmap(bmp, x, y, null);
			}
		}
	}
}
