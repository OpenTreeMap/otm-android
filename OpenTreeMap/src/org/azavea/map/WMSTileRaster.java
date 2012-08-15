package org.azavea.map;

import java.util.List;

import org.azavea.otm.App;
import org.azavea.otm.rest.handlers.TileHandler;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

public class WMSTileRaster extends SurfaceView {
	private Tile[][] tiles;
	private int numTilesX;
	private int numTilesY;
	private int tileWidth;
	private int tileHeight;
	private TileProvider tileProvider;
	
	private Paint paint;
	private MapView activityMapView;
	private WindowManager activityWindowManager;
	private GeoPoint topLeft;
	private GeoPoint bottomRight;
	private boolean initialized;
	private int initialTouchX;
	private int initialTouchY;
	private int panOffsetX;
	private int panOffsetY;
	
	public WMSTileRaster(Context context) throws Exception {
		super(context);
		init();
	}
	
	public WMSTileRaster(Context context, AttributeSet attrs) throws Exception {
		super(context, attrs);
		init();
	}
	
	public WMSTileRaster(Context context, AttributeSet attrs, int defStyle) throws Exception {
		super(context, attrs, defStyle);
		init();
	}
	
	public void setMapView(WindowManager windowManager, MapView mapView) {
		this.activityWindowManager = windowManager;
		this.activityMapView = mapView;
		
		Display display = activityWindowManager.getDefaultDisplay();
		int screenHeight = display.getHeight();
		int screenWidth = display.getWidth();
		tileHeight = screenHeight/(numTilesY - 2);
		tileWidth = screenWidth/(numTilesX - 2);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
			initialTouchX = (int)event.getRawX();
			initialTouchY = (int)event.getRawY();
			activityMapView.onTouchEvent(event);
		}
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
			panOffsetX -= initialTouchX - (int)event.getRawX();
			panOffsetY -= initialTouchY - (int)event.getRawY();
			initialTouchX = (int)event.getRawX();
			initialTouchY = (int)event.getRawY();
			activityMapView.onTouchEvent(event);
		}
		//mapView.onTouchEvent(event);
		return true; // Must be true or ACTION_MOVE isn't detected hence the
					 // need to manually pass the event to the MapView beforehand
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (activityMapView != null) {			

			if (!initialized) {
				initialSetup();
			}

			int shuffleRight = determineRightShuffle();
			
			int shuffleDown = determineShuffleDown();
			
			if ((shuffleRight != 0 || shuffleDown != 0)) {
				synchronized (this) {
					shuffleTiles(shuffleRight, shuffleDown);
					tileProvider.moveViewport(shuffleRight, shuffleDown);
					refreshTiles();
					
					updatePanOffsetX();

					updatePanOffsetY();
				}
			}
			
			drawTiles(canvas, panOffsetX, panOffsetY);
			initialized = true;
		}
	}

	private void init() throws Exception {
		initialized = false;
		activityMapView = null;
		initialTouchX = 0;
		initialTouchY = 0;
		panOffsetX = 0;
		panOffsetY = 0;
		
		SharedPreferences prefs = App.getSharedPreferences();
		int numTilesWithoutBorderX = Integer.parseInt(prefs.getString("num_tiles_x", "0"));
		int numTilesWithoutBorderY = Integer.parseInt(prefs.getString("num_tiles_y", "0"));		

		if (numTilesWithoutBorderX == 0 || numTilesWithoutBorderY == 0) {
			throw new Exception("Invalid value(s) for num_tiles_x and/or num_tiles_y");
		}

		// Add border
		numTilesX = numTilesWithoutBorderX + 2;
		numTilesY = numTilesWithoutBorderY + 2;
		
		paint = new Paint();
		paint.setAlpha(0x888);

		setWillNotDraw(false);
	}

	private void loadTiles() {
		tileProvider = new TileProvider(topLeft, bottomRight, numTilesX, numTilesY, tileWidth, tileHeight); 
		tiles = new Tile[numTilesX][numTilesY];
		for(int x=0; x<numTilesX; x++) {
			tiles[x] = new Tile[numTilesY];
			for(int y=0; y<numTilesY; y++) {
				tileProvider.getTile(x-1, y-1, new TileHandler(x, y) {
					@Override
					public void tileImageReceived(int x, int y, Bitmap image) {
						if (image != null) {
							tiles[x][y] = new Tile(image);
						}
					}
				});
			}
		}
	}
	
	private void drawTiles(Canvas canvas, int offsetX, int offsetY) {
		for(int x=0; x<numTilesX; x++) {
			for(int y=0; y<numTilesY; y++) {
				if (tiles[x][y] != null) {
					tiles[x][y].draw(canvas, (x-1) * tileWidth, (y-1) * -1 * tileHeight, offsetX, offsetY);
				}
			}
		}
	}
	
	private void updatePanOffsetY() {
		if (panOffsetY >= tileHeight) {
			panOffsetY = panOffsetY - tileWidth;
		}
		
		if (panOffsetY <= -tileWidth) {
			panOffsetY = panOffsetY + tileWidth;
		}
	}

	private void updatePanOffsetX() {
		if (panOffsetX >= tileWidth) {
			panOffsetX = panOffsetX - tileWidth;
		}
		
		if (panOffsetX <= -tileWidth) {
			panOffsetX = panOffsetX + tileWidth;
		}
	}

	private int determineShuffleDown() {
		int shuffleDown = 0;
		if (panOffsetY > tileHeight) {
			shuffleDown = 1;
		}
		
		if (panOffsetY < -tileHeight) {
			shuffleDown = -1;
		}
		return shuffleDown;
	}

	private int determineRightShuffle() {
		int shuffleRight = 0;
		if (panOffsetX > tileWidth) {
			shuffleRight = -1;
		}
		
		if (panOffsetX < -tileWidth) {
			shuffleRight = 1;
		}
		return shuffleRight;
	}

	private void initialSetup() {
		Projection proj = activityMapView.getProjection();
		topLeft = proj.fromPixels(activityMapView.getLeft(), activityMapView.getTop() + 800);
		bottomRight = proj.fromPixels(activityMapView.getLeft() + 480, activityMapView.getTop());
		loadTiles();
		this.postInvalidate();
	}
	
	private void shuffleTiles(int x, int y) {
            Tile[][] newTiles = new Tile[numTilesX][numTilesY];
            
            for(int k=0; k<numTilesX; k++) {
                    newTiles[k] = new Tile[numTilesY];
            }

            for(int i=0; i<numTilesX; i++) {
                    for(int j=0; j<numTilesY; j++) {
                            if (i+x<3 && i+x>=0 && j+y<3 && j+y>=0) {
                                    newTiles[i][j] = tiles[i+x][j+y];
                            }
                            else {
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
					tiles[i][j] = tileProvider.getTile(i-1, j-1);

// It seems better to block the UI during new-tile load at the moment but
// this can easily be switched over by commenting out the line above
// and un-commenting the following block.

//					tileProvider.getTile(i, j, new TileHandler(i-1, j-1) {
//						@Override
//						public void tileImageReceived(int x, int y, Bitmap image) {
//							tiles[x+1][y+1] = new Tile(x, y, image);
//						}
//					});
				}
			}
		}
	}
}
