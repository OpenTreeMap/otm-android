package org.azavea.otm.ui;

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
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.azavea.map.TMSTileProvider;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Geometry;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.PlotContainer;
import org.azavea.otm.data.Tree;
import org.azavea.otm.map.FallbackGeocoder;
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
import android.location.LocationListener;
import android.location.LocationManager;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainMapActivity extends MapActivity{
    private static LatLng START_POS;
    private static final int STREET_ZOOM_LEVEL = 17;
    private static final int FILTER_INTENT = 1;
    private static final int INFO_INTENT = 2;

    // modes for the add tree marker feature
    private static final int STEP1 = 1;
    private static final int STEP2 = 2;
    private static final int CANCEL = 3;
    private static final int FINISH = 4;

    private TextView plotSpeciesView;
    private TextView plotAddressView;
    private ImageView plotImageView;
    private RelativeLayout plotPopup;
    private Plot currentPlot; // The Plot we're currently showing a pop-up for, if any
    private Marker plotMarker;
    private GoogleMap mMap;
    private TextView filterDisplay;

    TMSTileProvider filterTileProvider;
    TMSTileProvider canopyTileProvider;
    TileOverlay filterTileOverlay;
    TileOverlay canopyTileOverlay;
    TileOverlay boundaryTileOverlay;

    private Location currentLocation;
    
    private String fullSizeTreeImageUrl = null;

    // Map click listener for normal view mode
    private OnMapClickListener showPopupMapClickListener = new GoogleMap.OnMapClickListener() {    
        @Override
        public void onMapClick(LatLng point) {
            Log.d("TREE_CLICK", "(" + point.latitude + "," + point.longitude + ")");

            final ProgressDialog dialog = ProgressDialog.show(MainMapActivity.this, "",
                    "Loading. Please wait...", true);
            dialog.show();

            final RequestGenerator rg = new RequestGenerator();
            RequestParams activeFilters = null;//App.getFilterManager().getActiveFiltersAsNearestPlotRequestParams();

            rg.getPlotsNearLocation(
                point.latitude,
                point.longitude,
                activeFilters,
                new ContainerRestHandler<PlotContainer>(new PlotContainer()) {

                    @Override
                    public void onFailure(Throwable e, String message) {
                        dialog.hide();
                        Log.e("TREE_CLICK",
                                "Error retrieving plots on map touch event: ", e);
            		}
        	
                    @Override
                    public void dataReceived(PlotContainer response) {
                        try {
                            Plot plot = response.getFirst();
                            if (plot != null) {
                                showPopup(plot);
                            } else {
                                hidePopup();
                			}
                        } catch (JSONException e) {
                            Log.e("TREE_CLICK",
                                    "Error retrieving plot info on map touch event: ", e);
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

    /*******************************************************
     * Overrides for the Activity base class
     *******************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupLocationUpdating(MainMapActivity.this);
        START_POS = App.getStartPos();
        setContentView(R.layout.activity_map_display_2);
        bindEnterKeyListenerToLocationSearchBar();
        filterDisplay = (TextView)findViewById(R.id.filterDisplay);
        setUpMapIfNeeded();
        plotPopup = (RelativeLayout) findViewById(R.id.plotPopup);
        setPopupViews();
        clearTileCache();
        if (plotPopup.getVisibility() == View.VISIBLE) {
            findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);	
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setTreeAddMode(CANCEL);
        clearTileCache();
    
        if (plotPopup.getVisibility() == View.VISIBLE) {
            findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);	
        }
    	
    }

    @Override 
     public void onActivityResult(int requestCode, int resultCode, Intent data) {     
       super.onActivityResult(requestCode, resultCode, data); 
       switch(requestCode) { 
           case FILTER_INTENT: 
               if (resultCode == Activity.RESULT_OK) { 
                   RequestParams activeFilters = App.getFilterManager().getActiveFiltersAsCqlRequestParams();
                   setFilterDisplay(App.getFilterManager().getActiveFilterDisplay());
                filterTileProvider.clearParameters();

                   if (activeFilters.toString().length() > 0) {
                       filterTileProvider.setParameter("test", "test");
               	}
               filterTileOverlay.clearTileCache();
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
    
    @Override
    public void onBackPressed() {
        hidePopup();
        setTreeAddMode(CANCEL);
    }
    
    /*********************************************
     * Event handlers bound to the view.
     *********************************************/
    public void handlePhotoDetailClick(View view) {
        if (fullSizeTreeImageUrl != null) {
            showPhotoDetail(fullSizeTreeImageUrl);
        }
    }
  
    public void handleLocationSearchClick(View view) {
        doLocationSearch();
    }
  
    // click handler for filter button
    public void doFilter(View view) {
        Intent filter = new Intent(this, FilterDisplay.class);
        startActivityForResult(filter, FILTER_INTENT);
    }

    // click handler for add tree button
    public void doAddTree(View view) {
        findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);
        if(App.getLoginManager().isLoggedIn()) {
            setTreeAddMode(CANCEL);
            setTreeAddMode(STEP1);
        } else {
            startActivity(new Intent(MainMapActivity.this, LoginActivity.class));
        }
    }
 
    //click handler for the next button
    public void submitNewTree(View view) {
        setTreeAddMode(FINISH);
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
    
    // onClick handler for "My Location" button
    public void showMyLocation(View view) {
        boolean success = false;
        if (currentLocation != null) {
            zoomMapToLocation(currentLocation);
            success = true;
        } else {
            Location cachedLocation = getCachedLocation();
            if (cachedLocation != null) {
                zoomMapToLocation(cachedLocation);
                success =true;
            }
        }
        
        if (success == false) {
            Toast.makeText(MainMapActivity.this, "Could not determine current location.", Toast.LENGTH_LONG).show();    		
        } 
    }
    
        
    /*********************************
     * Private methods
     *********************************/
    
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
        SharedPreferences prefs = App.getSharedPreferences();
        int startingZoomLevel = Integer.parseInt(prefs.getString("starting_zoom_level", "12"));
        String baseTileUrl = prefs.getString("tiler_url", null);
        String plotFeature = prefs.getString("plot_feature", null);
        String boundaryFeature = prefs.getString("boundary_feature", null);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(START_POS, startingZoomLevel));  
        mMap.getUiSettings().setZoomControlsEnabled(false);
        
        TileProvider boundaryTileProvider;
        try {
            boundaryTileProvider = new TMSTileProvider(baseTileUrl, boundaryFeature);
            boundaryTileOverlay = mMap.addTileOverlay(
                    new TileOverlayOptions().tileProvider(boundaryTileProvider).zIndex(0));

            // Canopy layer shows all trees, is always on, but is 'dimmed'
            // while a filter is active
            canopyTileProvider = new TMSTileProvider(baseTileUrl, plotFeature);
            //canopyTileOverlay = mMap.addTileOverlay(
            //        new TileOverlayOptions().tileProvider(canopyTileProvider).zIndex(50));

            // Set up the filter layer
            filterTileProvider = new TMSTileProvider(baseTileUrl, plotFeature);
            filterTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(filterTileProvider));
            filterTileProvider.setRangeParameter("tree.diameter", "50", "1001");
           
            // Set up the default click listener
            mMap.setOnMapClickListener(showPopupMapClickListener);
            
            setTreeAddMode(CANCEL);
            setUpBasemapControls();
        } catch (Exception e) {
            // TODO: Toast
        }
   
    }
    
    
    private void showPopup(Plot plot) {
        findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);
        
        //set default text
        plotSpeciesView.setText(getString(R.string.species_missing));
        plotAddressView.setText(getString(R.string.address_missing));
        plotImageView.setImageResource(R.drawable.ic_action_search);
    	
        try {
            String addr = plot.getAddressStreet(); 
            if (addr != null && addr.length() != 0) {
                plotAddressView.setText(addr);
            }
            String speciesName = plot.getTitle();
            plotSpeciesView.setText(speciesName);
        
            showImageOnPlotPopup(plot);
            
            // TODO: PHOTOS
            //fullSizeTreeImageUrl = tree.getTreePhotoUrl();

            LatLng position = new LatLng(plot.getGeometry().getY(), plot.getGeometry().getX());
            if (mMap.getCameraPosition().zoom >= STREET_ZOOM_LEVEL) {
                mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
            } else {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,STREET_ZOOM_LEVEL));
        	}
            if (plotMarker != null) {
                plotMarker.remove();
        	}
            plotMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker)));
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Could not show tree popup", e);
        }
        currentPlot = plot;
        plotPopup.setVisibility(View.VISIBLE);
        findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);
    }

    private void hidePopup() {
        findViewById(R.id.filter_add_buttons).setVisibility(View.VISIBLE);
        RelativeLayout plotPopup = (RelativeLayout) findViewById(R.id.plotPopup);
        plotPopup.setVisibility(View.INVISIBLE);
        currentPlot = null;
    }

    private void setPopupViews() {
        plotSpeciesView = (TextView) findViewById(R.id.plotSpecies);
        plotAddressView = (TextView) findViewById(R.id.plotAddress);
        plotImageView = (ImageView) findViewById(R.id.plotImage);
    }

    private void showImageOnPlotPopup(Plot plot) throws JSONException {
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
    
     
     private void setFilterDisplay(String activeFilterDisplay) {
         if (activeFilterDisplay.equals("") || activeFilterDisplay == null) {
             filterDisplay.setVisibility(View.GONE);
         } else {
             filterDisplay.setText(getString(R.string.filter_display_label) + " " + activeFilterDisplay);
             filterDisplay.setVisibility(View.VISIBLE);
         }
    	
    }

     private void zoomMapToLocation(Location l) {
         mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                l.getLatitude(),
                l.getLongitude()
        ), STREET_ZOOM_LEVEL));
    
     }
     
     private Location getCachedLocation() {
         Context context = MainMapActivity.this;
        Criteria crit = new Criteria();
        crit.setAccuracy(Criteria.ACCURACY_FINE);
        LocationManager locationManager = 
                (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            String provider = locationManager.getBestProvider(crit, true);
        	
            if (provider != null) {
                Location loc = locationManager.getLastKnownLocation(provider);
                if (loc != null) {
                    return loc;
            	}
        	
            }
        }
        return null;
    	
     } 
        
     /* tree add modes:
      *     CANCEL : not adding a tree
      *  STEP1  : "Tap to add a tree"
      *  STEP2  : "Long press to move the tree into position, then click next"
      *  FINISH : Create tree and redirect to tree detail page.
      */
    public void setTreeAddMode(int step) {
        if (mMap == null) {
            return;
        }

        View step1 = findViewById(R.id.addTreeStep1);
        View step2 = findViewById(R.id.addTreeStep2);
        View filterAddButtons = findViewById(R.id.filter_add_buttons);
        switch (step) {
            case CANCEL:
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.GONE);
                mMap.setOnMapClickListener(showPopupMapClickListener);
                filterAddButtons.setVisibility(View.VISIBLE);
                break;
            case STEP1:
                hidePopup();
                if (plotMarker != null) {
                    plotMarker.remove();
                    plotMarker = null;
                }
                filterAddButtons.setVisibility(View.GONE);
                step2.setVisibility(View.GONE);
                step1.setVisibility(View.VISIBLE);
                /*if (plotMarker != null) {
                    plotMarker.remove();
                    plotMarker = null;
                }*/
                if (mMap != null) {
                    mMap.setOnMapClickListener(addMarkerMapClickListener);
                }
                break;
            case STEP2:
                hidePopup();
                filterAddButtons.setVisibility(View.GONE);
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

    private Plot getPlotForNewTree() throws JSONException, IOException {
            Plot newPlot = new Plot();
            Geometry newGeometry = new Geometry();
            double lat = plotMarker.getPosition().latitude;
            double lon = plotMarker.getPosition().longitude;
            newGeometry.setY(lat);
            newGeometry.setX(lon);
            newPlot.setGeometry(newGeometry);
        	
            List<Address> addresses = null;
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                addresses = geocoder.getFromLocation(lat, lon, 1);
            } catch (Exception e) {
                e.printStackTrace();
        	}
        	
            if ((addresses != null) && (addresses.size() != 0)) {
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
                newPlot.setAddress("No Address");
        	}
        	
            newPlot.setTree(new Tree());
            return newPlot;
     }    
    
    private void moveMapAndFinishGeocode(LatLng pos) {
        EditText et = (EditText)findViewById(R.id.locationSearchField);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, STREET_ZOOM_LEVEL));
        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(et.getWindowToken(), 0);	
    }
    
    private void alertGeocodeError() {
           Toast.makeText(MainMapActivity.this, "Location search error.", Toast.LENGTH_SHORT).show();	
    }
    
    JsonHttpResponseHandler handleGoogleGeocodeResponse = new JsonHttpResponseHandler() {
        public void onSuccess(JSONObject data) {
            LatLng pos = FallbackGeocoder.decodeGoogleJsonResponse(data);
            if (pos == null) {
                alertGeocodeError();
            } else {
                moveMapAndFinishGeocode(pos);
            }
        };
        protected void handleFailureMessage(Throwable arg0, String arg1) {
            alertGeocodeError();
        };
    };

    /* Read the location search field, geocode it, and zoom to the location. */
    public void doLocationSearch() {
        EditText et = (EditText)findViewById(R.id.locationSearchField);
        String address = et.getText().toString();
    
        if (address.equals("")) {
            Toast.makeText(MainMapActivity.this, "Enter an address in the search field to search.", Toast.LENGTH_SHORT).show();
            return;
        } 

        SharedPreferences prefs = App.getSharedPreferences();
        FallbackGeocoder geocoder = new FallbackGeocoder(
                MainMapActivity.this,
                Double.parseDouble(prefs.getString("search_bbox_lower_left_lat", "")),
                Double.parseDouble(prefs.getString("search_bbox_lower_left_lon", "")),
                Double.parseDouble(prefs.getString("search_bbox_upper_right_lat", "")),
                Double.parseDouble(prefs.getString("search_bbox_upper_right_lon", ""))
        );
        
        LatLng pos = geocoder.androidGeocode(address);    	
        
        if (pos == null) {
            geocoder.httpGeocode(address, handleGoogleGeocodeResponse);
        } else {
            moveMapAndFinishGeocode(pos);
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
        
    private void setupLocationUpdating(Context applicationContext) {
        LocationManager locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);
    	
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
        	}
        	
            @Override
            public void onProviderEnabled(String provider) {
                // TODO Auto-generated method stub
        		
        	}
        	
            @Override
            public void onProviderDisabled(String provider) {
                // TODO Auto-generated method stub
        		
        	}
        	
            @Override
            public void onStatusChanged(String provider, int status,
                    Bundle extras) {	}
        };
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2 * 60 * 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2 * 60 * 1000, 0, locationListener);			
        }
    	
    	
    }
    
    private void clearTileCache() {
        if (canopyTileOverlay != null) {
            canopyTileOverlay.clearTileCache();
        }
        
        if (filterTileOverlay != null) {
            filterTileOverlay.clearTileCache();
        }
    }
    
    private void bindEnterKeyListenerToLocationSearchBar() {
        EditText et = (EditText)findViewById(R.id.locationSearchField);
        et.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    doLocationSearch();
                    return true;
                } else {
                    return false;
            	}
        	}
        });
    }

     
}


 