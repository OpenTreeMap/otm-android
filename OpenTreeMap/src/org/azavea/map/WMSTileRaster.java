package org.azavea.map;

import java.util.List;

import org.azavea.otm.App;
import org.azavea.otm.rest.handlers.TileHandler;
import org.azavea.otm.ui.MapDisplay;

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

// WMSTileRaster maintains a grid of tiles,
// the center of which will be in the center
// of the screen at time of initialization.
//
// As the user pans around, that center tile
// (and any surrounding tiles that should still
// be drawn) will be relocated in the grid, a
// different tile then becoming the center one.
//
// Naturally this process results in missing tiles
// in regions of the grid that now cover previously
// undrawn parts of the map. These tiles are replaced
// automatically.
public class WMSTileRaster extends SurfaceView {
	private Tile[][] tiles;
	private int numTilesX;
	private int numTilesY;
	private int tileWidth;
	private int tileHeight;
	private TileProvider tileProvider;
	private Paint paint;
	private MapDisplay activityMapDisplay;
	private WindowManager activityWindowManager;
	private GeoPoint topLeft;
	private GeoPoint bottomRight;
	private boolean initialized;
	private int initialTouchX;
	private int initialTouchY;
	private int panOffsetX;
	private int panOffsetY;
	private int initialTilesLoaded;
	
	private static final int BORDER_WIDTH = 2;
	
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
	
	// Associate this WMSTileRaster with a
	// MapView
	//
	// *** This should be handled in
	//     layout XML via a custom attribute
	//     but that approach is proving elusive
	//     at the moment ***
	public void setMapView(WindowManager windowManager, MapDisplay mapDisplay) {
		this.activityWindowManager = windowManager;
		this.activityMapDisplay = mapDisplay;
		
		Display display = activityWindowManager.getDefaultDisplay();
		int screenHeight = display.getHeight();
		int screenWidth = display.getWidth();
		tileHeight = screenHeight/(numTilesY - BORDER_WIDTH);
		tileWidth = screenWidth/(numTilesX - BORDER_WIDTH);
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		handleActionDown(event);
		handleActionMove(event);
		return true; // Must be true or ACTION_MOVE isn't detected hence the
					 // need to manually pass the event to the MapView beforehand
	}

	private void handleActionDown(MotionEvent event) {
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
			initialTouchX = (int)event.getRawX();
			initialTouchY = (int)event.getRawY();
			if (initialized) {
				activityMapDisplay.getMapView().onTouchEvent(event);
			}
		}
	}

	private void handleActionMove(MotionEvent event) {
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE) {
			panOffsetX -= initialTouchX - (int)event.getRawX();
			panOffsetY -= initialTouchY - (int)event.getRawY();
			initialTouchX = (int)event.getRawX();
			initialTouchY = (int)event.getRawY();
			if (initialized) {
				activityMapDisplay.getMapView().onTouchEvent(event);
			}
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		if (activityMapDisplay != null) {
			if (!initialized) {
				Log.d("WMSTileRaster", "initialSetup()");
				initialSetup();
			}

			int shuffleRight = determineShuffleRight();
			
			int shuffleDown = determineShuffleDown();
			
			if ((shuffleRight != 0 || shuffleDown != 0)) {
				synchronized (this) {
					relocateTiles(shuffleRight, shuffleDown);
					tileProvider.moveViewport(shuffleRight, shuffleDown);
					refreshTiles();
					
					updatePanOffsetX();

					updatePanOffsetY();
				}
			}
			
			if (initialized) {
				drawTiles(canvas, panOffsetX, panOffsetY);
			}
		}
	}

	// Initialize this WMSTileRaster
	private void init() throws Exception {
		initialized = false;
		initialTilesLoaded = 0;
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
		numTilesX = numTilesWithoutBorderX + BORDER_WIDTH;
		numTilesY = numTilesWithoutBorderY + BORDER_WIDTH;
		
		paint = new Paint();
		paint.setAlpha(0x888);

		setWillNotDraw(false);
	}

	// Initial load of all tiles in grid
	private void loadTiles() {
		// Start of this request group so increment the global sequence-id.
		App.incTileRequestSeqId();

		tileProvider = new TileProvider(topLeft, bottomRight, numTilesX, numTilesY, tileWidth, tileHeight); 
		tiles = new Tile[numTilesX][numTilesY];
		for(int x=0; x<numTilesX; x++) {
			tiles[x] = new Tile[numTilesY];
			for(int y=0; y<numTilesY; y++) {
				createTileRequest(x, y);
			}
		}
	}

	// Issue a request for a tile at screen
	// coordinates x, y and handle the resultant
	// response
	private void createTileRequest(int x, int y) {
		tileProvider.getTile(x-1, y-1, new TileHandler(x, y) {
			@Override
			public void tileImageReceived(int x, int y, Bitmap image) {
				Log.d("WMSTileRaster", "handler called");
				if (image != null) {
					Log.d("WMSTileRaster", "image available");
					tiles[x][y] = new Tile(image);

					// Remove from queue
					TileRequestQueue tileRequests = App.getTileRequestQueue();
					tileRequests.removeTileRequest(this.getBoundingBox());
					
					initialTilesLoaded++;
					if (initialTilesLoaded == 9) {
						activityMapDisplay.getMapView().invalidate();
					}
				}
			}
		});
	}

	// Draw all tiles in grid at specified
	// offset
	private void drawTiles(Canvas canvas, int offsetX, int offsetY) {
		for(int x=0; x<numTilesX; x++) {
			for(int y=0; y<numTilesY; y++) {
				if (tiles[x][y] != null) {
					tiles[x][y].draw(canvas, (x-1) * tileWidth, (y-1) * -1 * tileHeight, offsetX, offsetY);
				}
			}
		}
	}

	// Called when center bounding-box
	// is moved so that panOffsetY will
	// be made relative to new bounding-box
	private void updatePanOffsetY() {
		if (panOffsetY >= tileHeight) {
			panOffsetY = panOffsetY - tileHeight;
		}
		
		if (panOffsetY <= -tileHeight) {
			panOffsetY = panOffsetY + tileHeight;
		}
	}

	// Called when center bounding-box
	// is moved so that panOffsetX will
	// be made relative to new bounding-box
	private void updatePanOffsetX() {
		if (panOffsetX >= tileWidth) {
			panOffsetX = panOffsetX - tileWidth;
		}
		
		if (panOffsetX <= -tileWidth) {
			panOffsetX = panOffsetX + tileWidth;
		}
	}

	// Get vertical direction in which to
	// relocate tiles in grid
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

	// Get horizontal direction in which to
	// relocate tiles in grid
	private int determineShuffleRight() {
		int shuffleRight = 0;
		if (panOffsetX > tileWidth) {
			shuffleRight = -1;
		}
		
		if (panOffsetX < -tileWidth) {
			shuffleRight = 1;
		}
		return shuffleRight;
	}

	// Initial bounding-box setup and
	// tile-load. This will result in
	// requests being generated for ALL
	// grid positions
	private void initialSetup() {
		if (activityMapDisplay != null) {
			MapView mv = activityMapDisplay.getMapView();
			if (mv != null) {
				Projection proj = mv.getProjection();
				topLeft = proj.fromPixels(mv.getLeft(), mv.getTop() + tileHeight);
				bottomRight = proj.fromPixels(mv.getLeft() + tileWidth, mv.getTop());
				loadTiles();
				this.postInvalidate();
				initialized = true;
			} else {
				initialized = false;
			}
		}
	}
	
	// Move usable existing tiles within grid and
	// make space for new ones
	private void relocateTiles(int x, int y) {
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
	
	// Load new tiles for spaces left in grid
	// by relocateTiles()
	private void refreshTiles() {
		// Set sequence-id for this request-group
		App.incTileRequestSeqId();
		
		for(int i=0; i<numTilesX; i++) {
			for(int j=0; j<numTilesY; j++) {
				if (tiles[i][j] == null) {
					tileProvider.getTile(i-1, j-1, new TileHandler(i, j) {
						@Override
						public void tileImageReceived(int x, int y, Bitmap image) {
							tiles[x][y] = new Tile(image);
							
							// Remove from queue
							TileRequestQueue tileRequests = App.getTileRequestQueue();
							tileRequests.removeTileRequest(this.getBoundingBox());

							activityMapDisplay.getMapView().invalidate();
						}
					});
				}
			}
		}
	}
}
