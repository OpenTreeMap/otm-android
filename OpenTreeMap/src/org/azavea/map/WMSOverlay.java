package org.azavea.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

public class WMSOverlay extends Overlay {
	private double top;
	private double left;
	private double bottom;
	private double right;
	private Bitmap tile;
	
	public WMSOverlay() {
		super();
	}
	
	public WMSOverlay(GeoPoint topLeft, GeoPoint bottomRight) {
		this();
		top = topLeft.getLongitudeE6() / 1E6;
		left = topLeft.getLatitudeE6() / 1E6;
		bottom = bottomRight.getLongitudeE6() / 1E6;
		right = bottomRight.getLatitudeE6() / 1E6;
	}
	
	public WMSOverlay(Bitmap tile) {
		this.tile = tile;
	}
	
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		Log.d("WMSOverlay", "drawing");
		super.draw(canvas, mapView, false);
		Paint paint = new Paint();
		paint.setAlpha(0x888);
		canvas.drawBitmap(tile, 0, 0, paint);
	}
	
	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		Log.d("WMSOverlay", "boolean draw()");
		return super.draw(canvas, mapView, false, when);
	}
}
