package org.azavea.otm.ui;

/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.joelapenna.foursquared.widget.SegmentedButton;
import com.joelapenna.foursquared.widget.SegmentedButton.OnClickListenerSegmentedButton;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.azavea.map.WMSTileProvider;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Geometry;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.PlotContainer;
import org.azavea.otm.data.Tree;
import org.azavea.otm.map.TileProviderFactory;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
//import android.location.LocationManager;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainMapActivity extends MapActivity{
	private static LatLng START_POS;
	private static final int DEFAULT_ZOOM_LEVEL = 12;
	private static final int FILTER_INTENT = 1;
	private static final int INFO_INTENT = 2;
	// modes for the add marker feature
	private static final int STEP1 = 1;
	private static final int STEP2 = 2;
	private static final int CANCEL = 3;
	private static final int FINISH = 4;
	
	private TextView plotSpeciesView;
	private TextView plotAddressView;
	private TextView plotDiameterView;
	private TextView plotUpdatedByView;
	private ImageView plotImageView;
	private RelativeLayout plotPopup;
	private Plot currentPlot; // The Plot we're currently showing a pop-up for, if any
	private Marker plotMarker;
    private GoogleMap mMap;
    private TextView filterDisplay;

    TileOverlay filterTileOverlay;
    WMSTileProvider filterTileProvider;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        START_POS = App.getStartPos();
        setContentView(R.layout.activity_map_display_2);
        filterDisplay = (TextView)findViewById(R.id.filterDisplay);
        setUpMapIfNeeded();
		plotPopup = (RelativeLayout) findViewById(R.id.plotPopup);
		setPopupViews();
	}

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setTreeAddMode(CANCEL);
    }

    
    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView
     * MapView}) will show a prompt for the user to install/update the Google Play services APK on
     * their device.
     * <p>
     * A user can return to this Activity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the Activity may not have been
     * completely destroyed during this process (it is likely that it would only be stopped or
     * paused), {@link #onCreate(Bundle)} may not be called again so we should call this method in
     * {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            } else {
            	Toast.makeText(MainMapActivity.this, "Google Play store support is required to run this app.", Toast.LENGTH_LONG).show();
        		Log.e(App.LOG_TAG, "Map was null!");
            }
        }       
    }
    
    private void setUpMap() {
    	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(START_POS, DEFAULT_ZOOM_LEVEL));  
    	
    	/* This is the base tree layer using wms/geoserver for debugging purposes.
    	   In production we use tilecache.
    	
    	TileProvider wmsTileProvider = TileProviderFactory.getWmsTileProvider();
        TileOverlay wmsTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(wmsTileProvider));
        wmsTileOverlay.setZIndex(50);
        
        */
        
    	TileProvider treeTileProvider = TileProviderFactory.getTileCacheTileProvider();
    	TileOverlay treeTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(treeTileProvider));
    	treeTileOverlay.setZIndex(50);
    	
        // Set up the filter layer
        filterTileProvider = TileProviderFactory.getFilterLayerTileProvider();
        filterTileProvider.setCql("1=0");
        filterTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(filterTileProvider));
        filterTileOverlay.setZIndex(100);
       
        // Set up the default click listener
        mMap.setOnMapClickListener(showPopupMapClickListener);
        
        setTreeAddMode(CANCEL);
		setUpBasemapControls();
   
    }
    
    public void showPopup(Plot plot) {
		//set default text
		plotDiameterView.setText(getString(R.string.dbh_missing));
		plotSpeciesView.setText(getString(R.string.species_missing));
		plotAddressView.setText(getString(R.string.address_missing));
		plotImageView.setImageResource(R.drawable.ic_action_search);
		
		try {
	        plotUpdatedByView.setText("By " + plot.getLastUpdatedBy());
	        if (plot.getAddress().length() != 0) {
	        	plotAddressView.setText(plot.getAddress());
	        }
			Tree tree = plot.getTree();
			if (tree != null) {
				String speciesName;
				try {
					speciesName = tree.getSpeciesName();
				} catch (JSONException e) {
					speciesName = "No species name";
				}
				plotSpeciesView.setText(speciesName);
			
				if (tree.getDbh() != 0) {
					plotDiameterView.setText(String.valueOf(tree.getDbh()) +  
							getString(R.string.default_measure_units) + " " + 
							"Diameter");
				} 
				showImage(plot);
			}
			LatLng position = new LatLng(plot.getGeometry().getLat(), plot.getGeometry().getLon());				
			mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
			if (plotMarker != null) {
				plotMarker.remove();
			}
			plotMarker = mMap.addMarker(new MarkerOptions()
				.position(position)
				.title("")
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker)));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		currentPlot = plot;
		plotPopup.setVisibility(View.VISIBLE);
	}

	public void hidePopup() {
		RelativeLayout plotPopup = (RelativeLayout) findViewById(R.id.plotPopup);
		plotPopup.setVisibility(View.INVISIBLE);
		currentPlot = null;
	}

	private void setPopupViews() {
    	plotSpeciesView = (TextView) findViewById(R.id.plotSpecies);
    	plotAddressView = (TextView) findViewById(R.id.plotAddress);
    	plotDiameterView = (TextView) findViewById(R.id.plotDiameter);
    	plotUpdatedByView = (TextView) findViewById(R.id.plotUpdatedBy);
    	plotImageView = (ImageView) findViewById(R.id.plotImage);
    }

	private void showImage(Plot plot) throws JSONException {
		plot.getTreePhoto(new BinaryHttpResponseHandler(Plot.IMAGE_TYPES) {
			@Override
			public void onSuccess(byte[] imageData) {
				Bitmap scaledImage = Plot.createTreeThumbnail(imageData);
				ImageView plotImage = (ImageView) findViewById(R.id.plotImage);
				plotImage.setImageBitmap(scaledImage);
			}
			
			@Override
			public void onFailure(Throwable e, byte[] imageData) {
				// Log the error, but not important enough to bother the user
				Log.e(App.LOG_TAG, "Could not retreive tree image", e);
			}
		});
	}	  
    
        
    // onClick handler for tree-details pop-up touch event
 	public void showFullTreeInfo(View view) {
 		// Show TreeInfoDisplay with current plot
 		Intent viewPlot = new Intent(MainMapActivity.this, TreeInfoDisplay.class);
 		viewPlot.putExtra("plot", currentPlot.getData().toString());
 		
 		if (App.getLoginManager().isLoggedIn()) {
 			viewPlot.putExtra("user", App.getLoginManager().loggedInUser.getData().toString());
 		}
 		startActivityForResult(viewPlot, INFO_INTENT);
 	}
 	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_map_display, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
		if (itemId == R.id.menu_filter) {
			Intent filter = new Intent(this, FilterDisplay.class);
			startActivityForResult(filter, FILTER_INTENT);
		} else if (itemId == R.id.menu_add) {
			if(App.getLoginManager().isLoggedIn()) {
				setTreeAddMode(CANCEL);
				setTreeAddMode(STEP1);
			} else {
				startActivity(new Intent(MainMapActivity.this, LoginActivity.class));
			}
		}
        return true;
    }
    
    
 	@Override 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {     
 	  super.onActivityResult(requestCode, resultCode, data); 
 	  switch(requestCode) { 
 	  	case FILTER_INTENT: 
 	  		if (resultCode == Activity.RESULT_OK) { 
 	  			RequestParams activeFilters = App.getFilterManager().getActiveFiltersAsRequestParams();
 	  			setFilterDisplay(App.getFilterManager().getActiveFilterDisplay());
 	  			if (activeFilters.toString().equals("")) {
 	  				filterTileProvider.setCql("");
 	  				filterTileOverlay.clearTileCache();
 	  			} else {
 	  				RequestGenerator rc = new RequestGenerator();
 	  				rc.getCqlForFilters(activeFilters, handleNewFilterCql);
 	  			}
 	  		} 
 	  		break; 
 	  	case INFO_INTENT:
 	  		if (resultCode == TreeDisplay.RESULT_PLOT_EDITED) {
 	  	 
 	  			try {
 	  				// The plot was updated, so update the pop-up with any new data
 	  				Plot updatedPlot = new Plot();
					String plotJSON = data.getExtras().getString("plot");
					updatedPlot.setData(new JSONObject(plotJSON));
					showPopup(updatedPlot);
	 	  	 
		 	  	 } catch (JSONException e) {
		 	  		 		Log.e(App.LOG_TAG, "Unable to deserialze updated plot for map popup", e);
		 	  		 		hidePopup();
		 		 }
  			} else if (resultCode == TreeDisplay.RESULT_PLOT_DELETED) {
  				hidePopup();
  				// TODO: Do we need to refresh the map tile?
 	  	 	}
 	  	 break;
 	  } 
 	}
 	
 	private void setFilterDisplay(String activeFilterDisplay) {
 		if (activeFilterDisplay.equals("") || activeFilterDisplay == null) {
 			filterDisplay.setVisibility(View.GONE);
 		} else {
 			filterDisplay.setText(getString(R.string.filter_display_label) + " " + activeFilterDisplay);
 			filterDisplay.setVisibility(View.VISIBLE);
 		}
		
	}

	// onClick handler for "My Location" button
    public void showMyLocation(View view) {
    	
    	Context context = MainMapActivity.this;
    	LocationManager locationManager = 
    			(LocationManager) context.getSystemService(context.LOCATION_SERVICE);
    	if (locationManager != null) {
	    	Criteria crit = new Criteria();
			crit.setAccuracy(Criteria.ACCURACY_FINE);
	    	String provider = locationManager.getBestProvider(crit, true);
	    	if (provider != null) {
		    	Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
		    	if (lastKnownLocation != null) {
			    	mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
			    			lastKnownLocation.getLatitude(),
			    			lastKnownLocation.getLongitude()
			    	), DEFAULT_ZOOM_LEVEL+2));
		    	} else {
		    		Toast.makeText(MainMapActivity.this, "Could not determine current location.", Toast.LENGTH_SHORT).show();
		    	}
	    	}
    	} else {
    		Toast.makeText(MainMapActivity.this, "Could not determine current location.", Toast.LENGTH_SHORT).show();
    	}
    	
    }
    
    @Override
	public void onBackPressed() {
		hidePopup();
		setTreeAddMode(CANCEL);
	}

	// call backs for base layer switcher buttons
    public void hybridBaselayer(View view) {
    	mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }
    public void mapBaselayer(View view) {
    	mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);    	
    }
    public void satelliteBaselayer(View view) {
    	mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
    }
    
    // Map click listener for normal view mode
    private OnMapClickListener showPopupMapClickListener = new GoogleMap.OnMapClickListener() {	
    	@Override
		public void onMapClick(LatLng point) {		
			Log.d("TREE_CLICK", "(" + point.latitude + "," + point.longitude + ")");
			
			final ProgressDialog dialog = ProgressDialog.show(MainMapActivity.this, "", 
                  "Loading. Please wait...", true);
			dialog.show();
			
			final RequestGenerator rg = new RequestGenerator();
			rg.getPlotsNearLocation(
				point.latitude,
				point.longitude,
				new ContainerRestHandler<PlotContainer>(new PlotContainer()) {

					@Override
					public void onFailure(Throwable e, String message) {
						dialog.hide();
						Log.e("TREE_CLICK",
								"Error retrieving plots on map touch event: "
										+ e.getMessage());
						e.printStackTrace();
					}
			
					@Override
					public void dataReceived(PlotContainer response) {
						try {
							Plot plot = response.getFirst();
							if (plot != null) {
								Log.d("TREE_CLICK", plot.getData().toString());
								Log.d("TREE_CLICK", "Using Plot (id: " + plot.getId() + ") with coords X: " + plot.getGeometry().getLon() + ", Y:" + plot.getGeometry().getLat());
								showPopup(plot);
							} else {
								hidePopup();
							}
						} catch (JSONException e) {
							Log.e("TREE_CLICK",
									"Error retrieving plot info on map touch event: "
											+ e.getMessage());
							e.printStackTrace();
						} finally {
							dialog.hide();
						}
					}
				});
    	}
    };
    
    // Map click listener that allows us to add a tree
    private OnMapClickListener addMarkerMapClickListener = new GoogleMap.OnMapClickListener() {	
    	@Override
		public void onMapClick(LatLng point) {		
			Log.d("TREE_CLICK", "(" + point.latitude + "," + point.longitude + ")");
			
			plotMarker =  mMap.addMarker(new MarkerOptions()
		       .position(point)
		       .title("New Tree")
		       .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker))
		    );
			plotMarker.setDraggable(true);
			setTreeAddMode(STEP2);
    	}
    };
        
    public void setTreeAddMode(int step) {
    	if (mMap == null) {
    		return;
    	}
    	
    	View step1 =findViewById(R.id.addTreeStep1);
    	View step2 = findViewById(R.id.addTreeStep2);
    	switch (step) {
    		case CANCEL:
	    		step1.setVisibility(View.GONE);
	    		step2.setVisibility(View.GONE);
	    		if (plotMarker != null) {
	        		plotMarker.remove();
	        		plotMarker = null;
	        	}
	    		mMap.setOnMapClickListener(showPopupMapClickListener);
	    		break;
	    	case STEP1:
	    		step2.setVisibility(View.GONE);
	    		step1.setVisibility(View.VISIBLE);
	    		hidePopup();
	    		if (plotMarker != null) {
	        		plotMarker.remove();
	        		plotMarker = null;
	        	}
	            if (mMap != null) {
	            	mMap.setOnMapClickListener(addMarkerMapClickListener);
	            }
	    		break;
	    	case STEP2:
	    		hidePopup();
	    		step1.setVisibility(View.GONE);
	    		step2.setVisibility(View.VISIBLE);
	    		if (mMap != null) {
	    			mMap.setOnMapClickListener(null);
	    		}
	    		break;
	    	case FINISH:
	    		Intent editPlotIntent = new Intent (MainMapActivity.this, TreeEditDisplay.class);
	    		Plot newPlot;
	    		try {
	    			newPlot = getPlotForNewTree();
	    			String plotString = newPlot.getData().toString();
		    		editPlotIntent.putExtra("plot", plotString );
		    		editPlotIntent.putExtra("new_tree", "1");
		    		startActivity(editPlotIntent); 		
	    		} catch (Exception e) {
	    			e.printStackTrace();
	    			setTreeAddMode(CANCEL);
	    			Toast.makeText(MainMapActivity.this, "Error creating new tree", Toast.LENGTH_LONG).show();
	    		}
    	}
    }

    //click handler for the next button
    public void submitNewTree(View view) {
    	setTreeAddMode(FINISH);
    }
 
    private Plot getPlotForNewTree() throws JSONException, IOException {
    		Plot newPlot = new Plot();
			Geometry newGeometry = new Geometry();
			double lat = plotMarker.getPosition().latitude;
			double lon = plotMarker.getPosition().longitude;
			newGeometry.setLat(lat);
			newGeometry.setLon(lon);
			newPlot.setGeometry(newGeometry);
    		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
			List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);
			if (addresses.size() != 0) {
				Address addressData = addresses.get(0);
				String streetAddress = null;
				String city = null;
				String zip = null;
				if (addressData.getMaxAddressLineIndex() != 0) {
					streetAddress = addressData.getAddressLine(0);
				}
				if (streetAddress == null || streetAddress == "") {
					streetAddress = "No Address";
				}
				city = addressData.getLocality();
				zip = addressData.getPostalCode();
				
				newPlot.setAddressCity(city);
				newPlot.setAddressZip(zip);
				newPlot.setAddress(streetAddress);
			} else {
				return null;
			}
			
			newPlot.setTree(new Tree());
			
			return newPlot;
	 }

    // on response, set the global cqlFilter property, and cause the tile layer,
    // which has a reference to this property, to refresh.
    JsonHttpResponseHandler handleNewFilterCql = new JsonHttpResponseHandler() {
    	public void onSuccess(JSONObject data) {
    		String cqlFilterString = data.optString("cql_string");
       		Log.d("CQL-FILTERS", cqlFilterString);
    		filterTileProvider.setCql(cqlFilterString);
    		filterTileOverlay.clearTileCache();
    	};
    	protected void handleFailureMessage(Throwable arg0, String arg1) {
    		Toast.makeText(MainMapActivity.this, "Error processing filters", Toast.LENGTH_SHORT).show();
    		Log.e(App.LOG_TAG, arg1);
    		arg0.printStackTrace();
    		filterTileProvider.setCql("1=0");
    	};
    };
    
    public void handleLocationSearchClick(View view) {
    	EditText et = (EditText)findViewById(R.id.locationSearchField);
    	String address = et.getText().toString();
    	if (address == "") {
    		Toast.makeText(MainMapActivity.this, "Enter an address in the search field to search.", Toast.LENGTH_SHORT).show();
    	} else {
    		Geocoder g = new Geocoder(MainMapActivity.this);
    		try {
    			SharedPreferences prefs = App.getSharedPreferences();
    			double lowerLeftLatitude = Double.parseDouble(prefs.getString("search_bbox_lower_left_lat", ""));
    			double lowerLeftLongitude = Double.parseDouble(prefs.getString("search_bbox_lower_left_lon", ""));
    			double upperRightLatitude = Double.parseDouble(prefs.getString("search_bbox_upper_right_lat", ""));
    			double upperRightLongitude = Double.parseDouble(prefs.getString("search_bbox_upper_right_lon", ""));
    			
				List<Address> a = g.getFromLocationName(address, 1, lowerLeftLatitude, lowerLeftLongitude, upperRightLatitude, upperRightLongitude);
				if (a.size() == 0) {
					Toast.makeText(MainMapActivity.this, "Could not find that location.", Toast.LENGTH_SHORT).show();
				} else {
					Address geocoded = a.get(0);
					LatLng pos = new LatLng(geocoded.getLatitude(), geocoded.getLongitude());
			    	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, DEFAULT_ZOOM_LEVEL+4));  
				}
				 
			} catch (IOException e) {
				e.printStackTrace();
				Toast.makeText(MainMapActivity.this,  "Error searching for location.", Toast.LENGTH_SHORT);
			}
    	}
    }
    
    public void setUpBasemapControls() {
    	// Create the segmented buttons
        SegmentedButton buttons = (SegmentedButton)findViewById(R.id.basemap_controls);
        buttons.clearButtons();
        
        ArrayList<String> buttonNames = new ArrayList<String>();
        buttonNames.add("map");
        buttonNames.add("satellite");
        buttonNames.add("hybrid");
        buttons.addButtons(buttonNames.toArray(new String[buttonNames.size()]));
        
        buttons.setOnClickListener(new OnClickListenerSegmentedButton() {
            @Override
            public void onClick(int index) {
            	switch (index) {
            	case 0:
            		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); 
            		break;
            	case 1:
            	   	mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
            		break;
            	case 2:
            		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);           		
            		break;
            	}
            }
        });	
    }
}
 