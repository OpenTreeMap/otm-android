package org.azavea.map;

import org.azavea.otm.App;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.PlotContainer;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.azavea.otm.rest.handlers.TileHandler;
import org.azavea.otm.ui.MapDisplay;
import org.json.JSONException;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;
import android.R;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.ViewTreeObserver.OnTouchModeChangeListener;
import android.view.WindowManager;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

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
	private Projection proj;
	private WindowManager activityWindowManager;
	private GeoPoint topLeft;
	private GeoPoint bottomRight;
	private boolean initialized;
	private int initialTouchX;
	private int initialTouchY;
	private int panOffsetX;
	private int panOffsetY;
	private int initialTilesLoaded;
	private Bitmap touchIcon;
	private int numTilesOffsetX;
	private int numTilesOffsetY;
	
	private GeoPoint touchPoint;

	private static final int BORDER_WIDTH = 2;

	private int initialZoomLevel;

	private boolean scaledTiles;

	private ZoomManager zoomManager;

	private int zoomTolerance;

	private boolean zoomComplete;

	private long downTime;
	
	public WMSTileRaster(Context context) throws Exception {
		super(context);
		init();
	}

	public WMSTileRaster(Context context, AttributeSet attrs) throws Exception {
		super(context, attrs);
		init();
	}

	public WMSTileRaster(Context context, AttributeSet attrs, int defStyle)
			throws Exception {
		super(context, attrs, defStyle);
		init();
	}

	// Associate this WMSTileRaster with a
	// MapView
	//
	// *** This should be handled in
	// layout XML via a custom attribute
	// but that approach is proving elusive
	// at the moment ***
	public void setMapView(WindowManager windowManager, MapDisplay mapDisplay) {
		this.activityMapDisplay = mapDisplay;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);

		if (initialized) {
			MapView mv = activityMapDisplay.getMapView();
			mv.onTouchEvent(event);
			mv.getZoomButtonsController().onTouch(mv, event);
		}

		handleActionDown(event);
		handleActionUp(event);
		return true; // Must be true or ACTION_MOVE isn't detected hence the
						// need to manually pass the event to the MapView
						// beforehand
	}

	private void handleActionDown(MotionEvent event) {
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_DOWN) {
			// Store the time of this event.
			// We'll use it in handleActionUp
			// to determine if the user has been
			// panning or selecting.
			downTime = event.getEventTime();
		}
	}
	
	private void handleActionUp(MotionEvent event) {
		if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
			// Figure out of this event is the end of a move or not
			long upTime = event.getEventTime();
			if (upTime - downTime > 250) {
				return; // The user has been panning, not selecting a tree
			}
			
			// get the map coordinates for the touch location
			GeoPoint touchEvent = proj.fromPixels((int) event.getX(),
					(int) event.getY());
			Log.d(App.LOG_TAG, "Touch coords X: " + touchEvent.getLongitudeE6()
					+ ", Y:" + touchEvent.getLatitudeE6());
			
			final ProgressDialog dialog = ProgressDialog.show(activityMapDisplay, "", 
                    "Loading. Please wait...", true);
			dialog.show();
			
			final RequestGenerator rg = new RequestGenerator();
			rg.getPlotsNearLocation(
					touchEvent.getLatitudeE6() / 1E6,
					touchEvent.getLongitudeE6() / 1E6,
					new ContainerRestHandler<PlotContainer>(new PlotContainer()) {

						@Override
						public void onFailure(Throwable e, String message) {
							dialog.hide();
							invalidate();
							Log.e(App.LOG_TAG,
									"Error retrieving plots on map touch event: "
											+ e.getMessage());
							e.printStackTrace();
						}
				
						@Override
						public void dataReceived(PlotContainer response) {
							try {
								Plot plot = response.getFirst();
								if (plot != null) {
									Log.d(App.LOG_TAG, "Using Plot (id: " + plot.getId() + ") with coords X: " + plot.getGeometry().getLon() + ", Y:" + plot.getGeometry().getLat());
								
									double plotX = plot.getGeometry().getLonE6();
									double plotY = plot.getGeometry().getLatE6();
									
									touchPoint = new GeoPoint((int)plotY, (int)plotX);
									activityMapDisplay.showPopup(plot);
								
								} else {
									touchPoint = null;
									activityMapDisplay.hidePopup();
								}
							} catch (JSONException e) {
								Log.e(App.LOG_TAG,
										"Error retrieving plot info on map touch event: "
												+ e.getMessage());
								e.printStackTrace();
							} finally {
								dialog.hide();
								invalidate();
							}
						}
					});

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
				int mapHeight = this.getHeight();
				int mapWidth = this.getWidth();
				tileHeight = mapHeight / (numTilesY - 2);
				tileWidth = mapWidth / (numTilesX - 2);

				zoomManager = new ZoomManager(tileWidth, tileHeight,
						initialZoomLevel);

				initialSetup();
			}

			int shuffleRight = determineShuffleRight();
			Log.d(App.LOG_TAG, "shuffleRight = " + shuffleRight);

			int shuffleDown = determineShuffleDown();
			Log.d(App.LOG_TAG, "shuffleDown = " + shuffleDown);

			if ((shuffleRight != 0 || shuffleDown != 0)) {
				synchronized (this) {
					relocateTiles(shuffleRight, shuffleDown);

					tileProvider.moveViewport(shuffleRight, shuffleDown);
					refreshTiles();
				}
			}

			if (initialized) {
				if (zoomManager.getZoomFactor() <= zoomTolerance
						&& zoomManager.getZoomFactor() >= (1.0f / (float) zoomTolerance)) {
					drawTiles(canvas, (int)(panOffsetX - (zoomManager.getWidth() * numTilesOffsetX)), (int)(panOffsetY - (float)(zoomManager.getHeight() * numTilesOffsetY)));
					zoomComplete = false;
				} else {
					if (zoomComplete) {
						forceReInit();
						zoomComplete = false;
					}
				}
			}
			if (touchPoint != null) {
				// projection of the touch point into screen coordinates happens
				// here
				// because otherwise the point wouldn't move with the map tiles.

				// TODO: may want to check if the point is even on screen before
				// drawing
				Point point = proj.toPixels(touchPoint, null);
				float centerX = point.x - (touchIcon.getWidth() / 2.0f);
				float centerY = point.y - (touchIcon.getHeight() / 2.0f);
				canvas.drawBitmap(touchIcon, centerX, centerY, null);
			}
		}
	}

	public void forceReInit() {
		OTMMapView mv = activityMapDisplay.getMapView();

		initialZoomLevel = zoomManager.getZoomLevel();
		zoomManager.setInitialZoomLevel(initialZoomLevel);
		zoomManager.setZoomLevel(initialZoomLevel);

		// Reset pan offsets
		panOffsetX = 0;
		panOffsetY = 0;
		
		// Reset tile offsets
		numTilesOffsetX = 0;
		numTilesOffsetY = 0;

		// Reload tiles using new viewport
		initializeTiles(mv);
		this.postInvalidate();
	}

	private void initializeTiles(MapView mv) {
		Projection proj = mv.getProjection();
		topLeft = proj.fromPixels(mv.getLeft(), mv.getTop() + tileHeight);
		bottomRight = proj.fromPixels(mv.getLeft() + tileWidth, mv.getTop());
		loadTiles();
	}

	// Initialize this WMSTileRaster
	private void init() throws Exception {
		initialized = false;
		initialTilesLoaded = 0;
		initialTouchX = 0;
		initialTouchY = 0;
		panOffsetX = 0;
		panOffsetY = 0;
		initialZoomLevel = 14;
		scaledTiles = false;
		zoomTolerance = 2; // Allow one zoom-level before refreshing tiles from
							// server
		zoomComplete = false;
		
		numTilesOffsetX = 0;
		numTilesOffsetY = 0;

		SharedPreferences prefs = App.getSharedPreferences();
		int numTilesWithoutBorderX = Integer.parseInt(prefs.getString(
				"num_tiles_x", "0"));
		int numTilesWithoutBorderY = Integer.parseInt(prefs.getString(
				"num_tiles_y", "0"));

		if (numTilesWithoutBorderX == 0 || numTilesWithoutBorderY == 0) {
			throw new Exception(
					"Invalid value(s) for num_tiles_x and/or num_tiles_y");
		}

		// Add border
		numTilesX = numTilesWithoutBorderX + BORDER_WIDTH;
		numTilesY = numTilesWithoutBorderY + BORDER_WIDTH;

		paint = new Paint();
		paint.setAlpha(0x888);

		touchIcon = BitmapFactory.decodeResource(getResources(),
				R.drawable.star_on);

		setWillNotDraw(false);
	}

	// Initial load of all tiles in grid
	private void loadTiles() {
		// Start of this request group so increment the global sequence-id.
		App.incTileRequestSeqId();

		tileProvider = new TileProvider(topLeft, bottomRight, numTilesX,
				numTilesY, tileWidth, tileHeight);
		initialTilesLoaded = 0;

		tiles = new Tile[numTilesX][numTilesY];
		for (int x = 0; x < numTilesX; x++) {
			tiles[x] = new Tile[numTilesY];
			for (int y = 0; y < numTilesY; y++) {
				createTileRequest(x, y);
			}
		}
	}

	// Issue a request for a tile at screen
	// coordinates x, y and handle the resultant
	// response
	private void createTileRequest(int x, int y) {
		tileProvider.getTile(x - 1, y - 1, new TileHandler(x, y) {
			@Override
			public void tileImageReceived(int x, int y, Bitmap image) {
				if (image != null) {
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
		canvas.save();
		Matrix m = canvas.getMatrix();
		m.preScale(zoomManager.getZoomFactor(), zoomManager.getZoomFactor());
		canvas.setMatrix(m);
		for (int x = 0; x < numTilesX; x++) {
			for (int y = 0; y < numTilesY; y++) {
				if (tiles[x][y] != null) {
					tiles[x][y].draw(canvas,
							(int) ((x - 1) * zoomManager.getWidth()),
							(int) ((y - 1) * -1 * zoomManager.getHeight()),
							tileWidth, tileHeight, offsetX, offsetY,
							zoomManager.getZoomFactor(), scaledTiles);
				}
			}
		}
		canvas.restore();
	}

	// Get vertical direction in which to
	// relocate tiles in grid
	private int determineShuffleDown() {
		int shuffleDown = 0;
		
		int currentNumTilesOffset = panOffsetY / (int)zoomManager.getHeight();
		if (currentNumTilesOffset > numTilesOffsetY) {
			shuffleDown = 1;
			numTilesOffsetY++;
		}
		
		if (currentNumTilesOffset < numTilesOffsetY) {
			shuffleDown = -1;
			numTilesOffsetY--;
		}

		return shuffleDown;
	}

	// Get horizontal direction in which to
	// relocate tiles in grid
	private int determineShuffleRight() {
		int shuffleRight = 0;
			
		int currentNumTilesOffset = panOffsetX / (int)zoomManager.getWidth();
		if (currentNumTilesOffset > numTilesOffsetX) {
			shuffleRight = -1;
			numTilesOffsetX++;
		}
		
		if (currentNumTilesOffset < numTilesOffsetX) {
			shuffleRight = 1;
			numTilesOffsetX--;
		}
		
		return shuffleRight;
	}

	// Initial bounding-box setup and
	// tile-load. This will result in
	// requests being generated for ALL
	// grid positions
	private void initialSetup() {
		if (activityMapDisplay != null) {
			setupOnChangeListener();
			MapView mv = activityMapDisplay.getMapView();
			if (mv != null) {
				proj = mv.getProjection();
				topLeft = proj.fromPixels(mv.getLeft(), mv.getTop()
						+ tileHeight);
				bottomRight = proj.fromPixels(mv.getLeft() + tileWidth,
						mv.getTop());
				loadTiles();
				this.postInvalidate();
				initialized = true;
				initPanPolling();
			} else {
				initialized = false;
			}
		}
	}

	private void setupOnChangeListener() {
		activityMapDisplay.getMapView().setOnChangeListener(
				new MapViewChangeListener());
	}

	private void initPanPolling() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					OTMMapView mv = activityMapDisplay.getMapView();

					// Find out how far map is from our viewport
					Projection proj = mv.getProjection();
					Point overlayTopLeft = new Point();
					proj.toPixels(topLeft, overlayTopLeft);

					panOffsetX = overlayTopLeft.x;
					panOffsetY = (int) (overlayTopLeft.y - (mv.getHeight() * zoomManager
							.getZoomFactor()));
				}
			}
		}).start();
	}

	// Move usable existing tiles within grid and
	// make space for new ones
	private void relocateTiles(int x, int y) {
		Tile[][] newTiles = new Tile[numTilesX][numTilesY];

		for (int k = 0; k < numTilesX; k++) {
			newTiles[k] = new Tile[numTilesY];
		}

		for (int i = 0; i < numTilesX; i++) {
			for (int j = 0; j < numTilesY; j++) {
				if (i + x < numTilesX && i + x >= 0 && j + y < numTilesY
						&& j + y >= 0) {
					newTiles[i][j] = tiles[i + x][j + y];
				} else {
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

		for (int i = 0; i < numTilesX; i++) {
			for (int j = 0; j < numTilesY; j++) {
				if (tiles[i][j] == null) {
					tileProvider.getTile(i - 1, j - 1, new TileHandler(i, j) {
						@Override
						public void tileImageReceived(int x, int y, Bitmap image) {
							tiles[x][y] = new Tile(image);

							// Remove from queue
							TileRequestQueue tileRequests = App
									.getTileRequestQueue();
							tileRequests.removeTileRequest(this
									.getBoundingBox());

							activityMapDisplay.getMapView().invalidate();
						}
					});
				}
			}
		}
	}

	private class MapViewChangeListener implements OTMMapView.OnChangeListener {
		@Override
		public void onChange(MapView view, int newZoom, int oldZoom) {
			if (newZoom != oldZoom) {
				zoomManager.setZoomLevel(newZoom);

				zoomComplete = true;
				WMSTileRaster.this.invalidate();
			}
		}
	}
}
