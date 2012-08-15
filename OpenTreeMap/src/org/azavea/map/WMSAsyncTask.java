package org.azavea.map;

import java.util.List;

import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.util.Log;

public class WMSAsyncTask extends AsyncTask<Double, Void, Overlay> {
	private MapView mapView;
	private int x;
	private int y;
	
	public WMSAsyncTask(MapView mapView, int x, int y) {
		this.mapView = mapView;
		this.x = x;
		this.y = y;
	}
	
	protected Overlay doInBackground(Double... coords) {
		WMSClient wmsClient = new WMSClient();
		Bitmap tile = wmsClient.getTile(coords[0], coords[1], coords[2], coords[3]);
		WMSOverlay newOverlay = new WMSOverlay(tile);
		
		return newOverlay;
	}
	
	protected void onPostExecute(Overlay overlay) {
		Paint semiTransparent = new Paint();
		semiTransparent.setAlpha(0x888);
		if (mapView != null) {
			if (overlay != null) {
				Log.d("WMSAsyncTask", "Setting overlay");
				List<Overlay> overlays = mapView.getOverlays();
				overlays.clear();
				overlays.add(overlay);
				mapView.invalidate();
			}
		}
	}
}
