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
	private boolean okToShuffle;
	
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
		okToShuffle = true;
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
		
		for(int x=0; x<numTilesX; x++) {
			for(int y=0; y<numTilesY; y++) {
				if (tiles[x][y] != null) {
					tiles[x][y].draw(canvas, (x-1) * Tile.WIDTH, (y-1) * -1 * Tile.HEIGHT, offsetX, offsetY);
				}
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int offsetX = 0;
		int offsetY = 0;

		Log.d("OFFSETX", ""+panOffsetX);
		Log.d("OFFSETY", ""+panOffsetY);
		
		if (mapView != null) {			
			Projection proj = mapView.getProjection();
			this.projection = proj;

			if (!initialized) {
				topLeft = proj.fromPixels(mapView.getLeft(), mapView.getTop() + 800);
				bottomRight = proj.fromPixels(mapView.getLeft() + 480, mapView.getTop());
				loadTiles();
			}
			
			
			int shuffleRight = 0;
			int shuffleDown = 0;
			
			if (panOffsetX > Tile.WIDTH){
				shuffleRight = -1;
			}
			
			if (panOffsetX < -Tile.WIDTH) {
				shuffleRight = 1;
			}
			
			if (panOffsetY > Tile.HEIGHT) {
				shuffleDown = 1;
			}
			
			if (panOffsetY < -Tile.HEIGHT) {
				shuffleDown = -1;
			}
			
			if ((shuffleRight != 0 || shuffleDown != 0) && okToShuffle) {
				//okToShuffle = false;
				synchronized (this) {
					shuffleTiles(shuffleRight, shuffleDown);
					tileProvider.moveViewport(shuffleRight, shuffleDown);
					refreshTiles();
					panOffsetX = 0;
					panOffsetY = 0;
				}
				//okToShuffle = true;
			}
			
			drawTiles(canvas, offsetX + panOffsetX, offsetY + panOffsetY);
			initialized = true;
		}
	}
	
	private void displayTile(int x, int y, Tile t) {
		Log.d("Display Tile", "grid(" + x + "," + y +") -> x = " + t.getX() + ", y = " + t.getY());
	}
	
	private Bitmap loadTile() {
		WMSClient wmsClient = new WMSClient();
		return wmsClient.getTile(topLeft.getLongitudeE6()/1E6, topLeft.getLatitudeE6()/1E6,
				bottomRight.getLongitudeE6()/1E6, bottomRight.getLatitudeE6()/1E6);
	}
	
	public void shuffleTiles(int x, int y)
    {
            Tile[][] newTiles = new Tile[numTilesX][numTilesY];
            
            for(int k=0; k<numTilesX; k++)
            {
                    newTiles[k] = new Tile[numTilesY];
            }

            for(int i=0; i<numTilesX; i++)
            {
                    for(int j=0; j<numTilesY; j++)
                    {
                            if (i+x<3 && i+x>=0 && j+y<3 && j+y>=0)
                            {
                                    newTiles[i][j] = tiles[i+x][j+y];
                            }
                            else
                            {
                                    newTiles[i][j] = null;
                            }
                    }
            }
            tiles = newTiles;
    }       
	
	private void refreshTiles() {
		for(int i=0; i<numTilesX; i++) {
			for(int j=0; j<numTilesY; j++) {
				if (tiles[i][j] == null) {
					tiles[i][j] = tileProvider.getTile(i, j);
					Log.d("", "Tile " + i + "," + j + " gets screen coords " + tiles[i][j].getX() + "," + tiles[i][j].getY());
				}
			}
		}
	}
}
