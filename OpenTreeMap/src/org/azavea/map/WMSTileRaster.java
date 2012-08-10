package org.azavea.map;

import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class WMSTileRaster extends SurfaceView {
	private Tile[][] tiles;
	private int numTilesX;
	private int numTilesY;
	private TileProvider tileProvider;
	
	private Bitmap tile;
	private Paint paint;
	private MapView mapView;
	private GeoPoint topLeft;
	private GeoPoint bottomRight;
	private int screenOffsetX;
	private int screenOffsetY;
	private Projection projection;
	private boolean initialized;
	private int initialTouchX;
	private int initialTouchY;
	private int panOffsetX;
	private int panOffsetY;
	
	public WMSTileRaster(Context context) {
		super(context);
		init();
	}
	
	public WMSTileRaster(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}
	
	public WMSTileRaster(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setMapView(MapView mapView) {
		this.mapView = mapView;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
			initialTouchX = (int)event.getRawX();
			initialTouchY = (int)event.getRawY();
		}
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
			panOffsetX -= initialTouchX - (int)event.getRawX();
			panOffsetY -= initialTouchY - (int)event.getRawY();
			initialTouchX = (int)event.getRawX();
			initialTouchY = (int)event.getRawY();
		}
		mapView.onTouchEvent(event);
		return true; // Must be true or ACTION_MOVE isn't detected hence
					 // needing to manually pass the event to the MapView beforehand
	}
	
	private void init() {
		initialized = false;
		mapView = null;
		screenOffsetX = 480; // Location of center-tile
		screenOffsetY = 800;
		initialTouchX = 0;
		initialTouchY = 0;
		panOffsetX = 0;
		panOffsetY = 0;
		
		numTilesX = 1+2;
		numTilesY = 1+2;
//		topLeft = new GeoPoint((int)(-75.165708*1E6), (int)(39.952622*1E6));
//		bottomRight = new GeoPoint((int)(-75.124510*1E6), (int)(40.005234*1E6));
//		topLeft = new GeoPoint((int)(39976432), (int)(-75186307));
//		bottomRight = new GeoPoint((int)(39928769), (int)(-75145109));

		
		//loadTiles();
		
		paint = new Paint();
		paint.setAlpha(0x888);
		setWillNotDraw(false);
	}

	private void loadTiles() {
		tileProvider = new TileProvider(topLeft, bottomRight, numTilesX, numTilesY, mapView.getProjection()); 
		tiles = new Tile[numTilesX][numTilesY];
		for(int x=0; x<numTilesX; x++) {
			tiles[x] = new Tile[numTilesY];
			for(int y=0; y<numTilesY; y++) {
				tiles[x][y] = tileProvider.getTile(x, y);
			}
		}
	}
	
	private void drawTiles(Canvas canvas, int offsetX, int offsetY) {
		Log.d("WMSTileRaster", "Drawing tiles...");
//		if (mapView != null) {
//			Projection proj = mapView.getProjection();
//			topLeft = proj.fromPixels(mapView.getLeft(), mapView.getTop());
//			bottomRight = proj.fromPixels(mapView.getRight(), mapView.getBottom());
//			Log.d("WMSTileRaster", topLeft.getLatitudeE6() + ", " + topLeft.getLongitudeE6());
//			Log.d("WMSTileRaster", bottomRight.getLatitudeE6() + ", " + bottomRight.getLongitudeE6());
//		}
		
		for(int x=0; x<numTilesX; x++) {
			for(int y=0; y<numTilesY; y++) {
				if (tiles[x][y] != null) {
					tiles[x][y].draw(canvas, offsetX, offsetY);
				}
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		// Works for single tile
		///////////////////////////////////////////////////////////////////////////////////////////
//		if (mapView != null) {
//			Log.d("WMSTileRaster", "mapView is not null");
//			Projection proj = mapView.getProjection();
////			int[] mapLocation = new int[2];
////			mapView.getLocationOnScreen(mapLocation);
////			Log.d("WMSTileRaster", "mapView = " + mapLocation[0] + ", " + mapLocation[1]);
//			GeoPoint topLeft = proj.fromPixels(mapView.getLeft(), mapView.getTop() + 800);
//			GeoPoint bottomRight = proj.fromPixels(mapView.getLeft() + 480, mapView.getTop());
//
//			if (!topLeft.equals(this.topLeft) || !bottomRight.equals(this.bottomRight)) {
//				this.topLeft = topLeft;
//				this.bottomRight = bottomRight;
//				tile = loadTile();
//			}
//		}
//		
//		canvas.drawBitmap(tile, 0, 0, paint);
		//////////////////////////////////////////////////////////////////////////////////////////
		
		// PANNING
		// Need to know where mapview is in relation to our grid
//		if (mapView != null) {
//			Log.d("WMSTileRaster", "mapView is not null");
//			Projection proj = mapView.getProjection();
//			GeoPoint newTopLeft = proj.fromPixels(mapView.getLeft(), mapView.getTop());
//			
//			// Calculate geometric offset
//			GeoPoint offset = new GeoPoint(topLeft.getLatitudeE6() - newTopLeft.getLatitudeE6(), topLeft.getLongitudeE6() - newTopLeft.getLongitudeE6());
//			
//			// Figure out which tile topLeft of viewport is in
//			
//			
//			// How far is topleft of viewport from that tile?
//			
//			// If it's within threshold, reinitialize the view
//		}

		int offsetX = 0;
		int offsetY = 0;
		
		if (mapView != null) {			
			Projection proj = mapView.getProjection();
			this.projection = proj;

			if (!initialized) {
				topLeft = proj.fromPixels(mapView.getLeft(), mapView.getTop() + 800);
				bottomRight = proj.fromPixels(mapView.getLeft() + 480, mapView.getTop());
				loadTiles();
			}
			drawTiles(canvas, offsetX + panOffsetX, offsetY + panOffsetY);
			initialized = true;
		}
	}
	
	private Bitmap loadTile() {
		WMSClient wmsClient = new WMSClient();
		return wmsClient.getTile(topLeft.getLongitudeE6()/1E6, topLeft.getLatitudeE6()/1E6,
				bottomRight.getLongitudeE6()/1E6, bottomRight.getLatitudeE6()/1E6);
//		return wmsClient.getTile(topLeft.getLatitudeE6()/1E6, topLeft.getLongitudeE6()/1E6,
//				bottomRight.getLatitudeE6()/1E6, bottomRight.getLongitudeE6()/1E6);
	}
}
