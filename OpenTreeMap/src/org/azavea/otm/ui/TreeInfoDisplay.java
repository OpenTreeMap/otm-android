package org.azavea.otm.ui;

import org.azavea.map.OTMMapView;
import org.azavea.otm.App;
import org.azavea.otm.FieldGroup;
import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TreeInfoDisplay extends MapActivity{
	private GeoPoint treeLocation;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plot_view_activity);
        LinearLayout fieldList = (LinearLayout)findViewById(R.id.field_list);
        LayoutInflater layout = ((Activity)this).getLayoutInflater();
        
        try {
        	Plot plot = new Plot();
			plot.setData(new JSONObject(getIntent().getStringExtra("plot")));

			treeLocation = getTreeLocation(plot);
			
			showPositionOnMap();

			setTreeHeaderValues(plot);
			
			for (FieldGroup group : App.getFieldManager().getFieldGroups()) {
				fieldList.addView(group.renderForDisplay(layout, plot));
			}
		} catch (JSONException e) {
			Toast.makeText(this, "Could not retrieve Tree information", 
					Toast.LENGTH_SHORT).show();
			Log.e(App.LOG_TAG, "Failed to create tree view", e);
		}
    }
    
    private GeoPoint getTreeLocation(Plot plot) {
    	try {
			double lon = plot.getGeometry().getLonE6();
			double lat = plot.getGeometry().getLatE6();
			return new GeoPoint((int)lat, (int)lon);
    	} catch (Exception e) {
    		return null;
    	}
    }
    
	private void showPositionOnMap() {
		OTMMapView mapView = (OTMMapView)findViewById(R.id.map_vignette);
		mapView.getOverlays().add(new TreeLocationOverlay());
		mapView.getController().animateTo(treeLocation);
		mapView.getController().setZoom(16);
	}

	private void setTreeHeaderValues(Plot plot) throws JSONException {
		setText(R.id.address, plot.getAddress());
		setText(R.id.species, plot.getTree().getSpeciesName());
		setText(R.id.updated_on, "Last updated on " + plot.getLastUpdated());
		setText(R.id.updated_by, "By " + plot.getLastUpdatedBy());
	}
	
	private void setText(int resourceId, String text) {
		// Only set the text if it exists, letting the layout define default text
		if (text != null &&  !"".equals(text)) {
			((TextView)findViewById(resourceId)).setText(text);
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
				mapView.getProjection().toPixels(treeLocation, point);
				
				Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.ic_forest2);
				int x = point.x - bmp.getWidth() / 2;
				int y = point.y - bmp.getHeight();
				
				canvas.drawBitmap(bmp, x, y, null);
			}
		}
	}
}
