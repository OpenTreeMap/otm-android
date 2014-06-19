package org.azavea.otm.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.azavea.map.FilterableTMSTileProvider;
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
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.joelapenna.foursquared.widget.SegmentedButton;
import com.joelapenna.foursquared.widget.SegmentedButton.OnClickListenerSegmentedButton;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainMapActivity extends Fragment {
    private static LatLng START_POS;
    private static final int STREET_ZOOM_LEVEL = 17;
    private static final int FILTER_INTENT = 1;
    private static final int INFO_INTENT = 2;
    private static final int ADD_INTENT = 3;

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
    private MapView mapView;
    private GoogleMap mMap;
    private TextView filterDisplay;

    private Location currentLocation;
    private LocationManager locationManager;
    private LocationListener locationListener;

    FilterableTMSTileProvider filterTileProvider;
    TileOverlay filterTileOverlay;
    TileOverlay canopyTileOverlay;
    TileOverlay boundaryTileOverlay;

    // Map click listener for normal view mode
    private final OnMapClickListener showPopupMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng point) {
            Log.d("TREE_CLICK", "(" + point.latitude + "," + point.longitude + ")");

            final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                    "Loading. Please wait...", true);
            dialog.show();

            final RequestGenerator rg = new RequestGenerator();
            RequestParams activeFilters = null;

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
    private final OnMapClickListener addMarkerMapClickListener = new GoogleMap.OnMapClickListener() {
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

    public void onBackPressed() {
        hidePopup();
        removePlotMarker();
        setTreeAddMode(CANCEL);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            disableLocationUpdating();
        } else {
            setupLocationUpdating(getActivity());
        }
    }

    /*******************************************************
     * Overrides for the Fragment base class
     *******************************************************/
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setupLocationUpdating(getActivity());

        final View view = inflater.inflate(R.layout.activity_map_display_2, container, false);

        MapsInitializer.initialize(getActivity());
        MapHelper.checkGooglePlay(getActivity());

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                "Loading Map Info...", true);

        Callback instanceLoaded = new Callback() {
            @Override
            public boolean handleMessage(Message result) {
                try {
                    if (result.getData().getBoolean("success")) {
                        START_POS = App.getCurrentInstance().getStartPos();
                        bindActionToLocationSearchBar(view);
                        filterDisplay = (TextView) view.findViewById(R.id.filterDisplay);
                        setUpMapIfNeeded();
                        plotPopup = (RelativeLayout) view.findViewById(R.id.plotPopup);
                        setPopupViews(view);
                        clearTileCache();
                        if (plotPopup.getVisibility() == View.VISIBLE) {
                            view.findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);
                        }
                        setupViewHandlers(view);
                    }
                    return true;

                } catch (Exception e) {
                    Log.e(App.LOG_TAG, "Unable to setup map", e);
                    Toast.makeText(App.getAppInstance(), "Unable to setup map", Toast.LENGTH_LONG).show();
                    return false;

                } finally {
                    dialog.dismiss();
                }
            }
        };
        // Check for an instance before loading the map
        App.getAppInstance().ensureInstanceLoaded(instanceLoaded);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        setupLocationUpdating(getActivity());
        MapHelper.checkGooglePlay(getActivity());
        mapView.onResume();
        if (App.getAppInstance().getCurrentInstance() != null) {
            setUpMapIfNeeded();
            setTreeAddMode(CANCEL);
            clearTileCache();

            if (plotPopup != null && plotPopup.getVisibility() == View.VISIBLE) {
                getActivity().findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case FILTER_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    Collection<JSONObject> activeFilters = App.getFilterManager().getActiveFilters();
                    setFilterDisplay(App.getFilterManager().getActiveFilterDisplay());

                    filterTileProvider.setParameters(activeFilters);
                    filterTileOverlay.clearTileCache();
                }
                break;
            case INFO_INTENT:
                if (resultCode == TreeDisplay.RESULT_PLOT_EDITED) {
                    showPlotFromIntent(data);
                } else if (resultCode == TreeDisplay.RESULT_PLOT_DELETED) {
                    hidePopup();
                    // TODO: Do we need to refresh the map tile?
                }
                break;

            case ADD_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    showPlotFromIntent(data);
                }
        }
    }

    private void showPlotFromIntent(Intent data) {
        try {
            // The plot was updated, so update the pop-up with any new data
            String plotJSON = data.getExtras().getString("plot");
            Plot updatedPlot = new Plot(new JSONObject(plotJSON));
            showPopup(updatedPlot);

        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Unable to deserialze updated plot for map popup", e);
            hidePopup();
        }
    }

    // If you use a MapView directly, you need to forward it events
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }
        disableLocationUpdating();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mapView != null) {
            mapView.onSaveInstanceState(outState);
        }
        super.onSaveInstanceState(outState);
    }

    /*********************************
     * Private methods
     *********************************/

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link com.google.android.gms.maps.MapView})
     * will show a prompt for the user to install/update the Google Play services APK on
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
            // Try to obtain the map from the MapView.
            mMap = mapView.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            } else {
                Toast.makeText(getActivity(), "Google Play store support is required to run this app.", Toast.LENGTH_LONG).show();
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
        String[] displayFilters = App.getAppInstance().getResources().getStringArray(R.array.display_filters);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(START_POS, startingZoomLevel));
        mMap.getUiSettings().setZoomControlsEnabled(false);

        try {
            TMSTileProvider boundaryTileProvider = new TMSTileProvider(baseTileUrl, boundaryFeature);
            boundaryTileOverlay = mMap.addTileOverlay(
                    new TileOverlayOptions().tileProvider(boundaryTileProvider).zIndex(0));

            // Canopy layer shows all trees, is always on, but is 'dimmed' while a filter is active
            TMSTileProvider canopyTileProvider = new TMSTileProvider(baseTileUrl, plotFeature, 76);
            canopyTileProvider.setDisplayParameters(Arrays.asList(displayFilters));
            canopyTileOverlay = mMap.addTileOverlay(
                    new TileOverlayOptions().tileProvider(canopyTileProvider).zIndex(50));

            // Set up the filter layer
            filterTileProvider = new FilterableTMSTileProvider(baseTileUrl, plotFeature);
            filterTileOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(filterTileProvider));
            filterTileProvider.setDisplayParameters(Arrays.asList(displayFilters));

            // Set up the default click listener
            mMap.setOnMapClickListener(showPopupMapClickListener);

            setTreeAddMode(CANCEL);
            setUpBasemapControls();
        } catch (Exception e) {
            // TODO: Toast
        }
    }

    private void setupViewHandlers(View view) {
        view.findViewById(R.id.plotImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainMapActivity.this.currentPlot != null) {
                    currentPlot.getTreePhoto(MapHelper.getPhotoDetailHandler(getActivity()));
                }
            }
        });

        view.findViewById(R.id.locationSearchButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLocationSearch();
            }
        });

        view.findViewById(R.id.filterButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent filter = new Intent(getActivity(), FilterDisplay.class);
                startActivityForResult(filter, FILTER_INTENT);
            }
        });

        view.findViewById(R.id.addTreeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);
                if(App.getLoginManager().isLoggedIn()) {
                    setTreeAddMode(CANCEL);
                    setTreeAddMode(STEP1);
                } else {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                }
            }
        });

        view.findViewById(R.id.treeAddNext).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setTreeAddMode(FINISH);
            }
        });

        view.findViewById(R.id.plotPopup).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show TreeInfoDisplay with current plot
                Intent viewPlot = new Intent(getActivity(), TreeInfoDisplay.class);
                viewPlot.putExtra("plot", currentPlot.getData().toString());

                if (App.getLoginManager().isLoggedIn()) {
                    viewPlot.putExtra("user", App.getLoginManager().loggedInUser.getData().toString());
                }
                startActivityForResult(viewPlot, INFO_INTENT);
            }
        });

        view.findViewById(R.id.mylocationbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                    Toast.makeText(getActivity(), "Could not determine current location.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showPopup(Plot plot) {
        getActivity().findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);

        //set default text
        plotSpeciesView.setText(getString(R.string.species_missing));
        plotAddressView.setText(getString(R.string.address_missing));
        plotImageView.setImageResource(R.drawable.missing_tree_photo);

        try {
            String addr = plot.getAddressStreet();
            if (!TextUtils.isEmpty(addr)) {
                plotAddressView.setText(addr);
            }
            String speciesName = plot.getTitle();
            plotSpeciesView.setText(speciesName);

            showImageOnPlotPopup(plot);

            LatLng position = zoomToPlot(plot);

            removePlotMarker();
            plotMarker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title("")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker)));
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Could not show tree popup", e);
        }
        currentPlot = plot;
        plotPopup.setVisibility(View.VISIBLE);
        getActivity().findViewById(R.id.filter_add_buttons).setVisibility(View.GONE);
    }

    private LatLng zoomToPlot(Plot plot) throws JSONException {
        LatLng position = new LatLng(plot.getGeometry().getY(), plot.getGeometry().getX());
        if (mMap.getCameraPosition().zoom >= STREET_ZOOM_LEVEL) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position,STREET_ZOOM_LEVEL));
        }
        return position;
    }

    private void hidePopup() {
        getActivity().findViewById(R.id.filter_add_buttons).setVisibility(View.VISIBLE);
        RelativeLayout plotPopup = (RelativeLayout) getActivity().findViewById(R.id.plotPopup);
        plotPopup.setVisibility(View.INVISIBLE);
        currentPlot = null;
    }

    private void removePlotMarker() {
        if (plotMarker != null) {
            plotMarker.remove();
        }
    }

    private void setPopupViews(View view) {
        plotSpeciesView = (TextView) view.findViewById(R.id.plotSpecies);
        plotAddressView = (TextView) view.findViewById(R.id.plotAddress);
        plotImageView = (ImageView) view.findViewById(R.id.plotImage);
    }

    private void showImageOnPlotPopup(Plot plot) throws JSONException {
        plot.getTreeThumbnail(new BinaryHttpResponseHandler(Plot.IMAGE_TYPES) {
            @Override
            public void onSuccess(byte[] imageData) {
                ImageView plotImage = (ImageView) getActivity().findViewById(R.id.plotImage);
                plotImage.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
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
        Context context = getActivity();
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

        View step1 = getActivity().findViewById(R.id.addTreeStep1);
        View step2 = getActivity().findViewById(R.id.addTreeStep2);
        View filterAddButtons = getActivity().findViewById(R.id.filter_add_buttons);
        switch (step) {
            case CANCEL:
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.GONE);
                mMap.setOnMapClickListener(showPopupMapClickListener);
                filterAddButtons.setVisibility(View.VISIBLE);
                break;
            case STEP1:
                hidePopup();
                removePlotMarker();
                filterAddButtons.setVisibility(View.GONE);
                step2.setVisibility(View.GONE);
                step1.setVisibility(View.VISIBLE);

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
                Intent editPlotIntent = new Intent (getActivity(), TreeEditDisplay.class);
                Plot newPlot;
                try {
                    newPlot = getPlotForNewTree();
                    String plotString = newPlot.getData().toString();
                    editPlotIntent.putExtra("plot", plotString );
                    editPlotIntent.putExtra("new_tree", "1");
                    startActivityForResult(editPlotIntent, ADD_INTENT);

                } catch (Exception e) {
                    e.printStackTrace();
                    setTreeAddMode(CANCEL);
                    Toast.makeText(getActivity(), "Error creating new tree", Toast.LENGTH_LONG).show();
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

            // We always get coordinates in lat/lon
            newGeometry.setSrid(4326);
            newPlot.setGeometry(newGeometry);

            List<Address> addresses = null;
            try {
                Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
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
        EditText et = (EditText)getActivity().findViewById(R.id.locationSearchField);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, STREET_ZOOM_LEVEL));
        InputMethodManager im = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        im.hideSoftInputFromWindow(et.getWindowToken(), 0);
    }

    private void alertGeocodeError() {
           Toast.makeText(getActivity(), "Location search error.", Toast.LENGTH_SHORT).show();
    }

    JsonHttpResponseHandler handleGoogleGeocodeResponse = new JsonHttpResponseHandler() {
        @Override
        public void onSuccess(JSONObject data) {
            LatLng pos = FallbackGeocoder.decodeGoogleJsonResponse(data);
            if (pos == null) {
                alertGeocodeError();
            } else {
                moveMapAndFinishGeocode(pos);
            }
        }
        @Override
        protected void handleFailureMessage(Throwable arg0, String arg1) {
            alertGeocodeError();
        }
    };

    /* Read the location search field, geocode it, and zoom to the location. */
    public void doLocationSearch() {
        EditText et = (EditText)getActivity().findViewById(R.id.locationSearchField);
        String address = et.getText().toString();

        if (address.equals("")) {
            Toast.makeText(getActivity(), "Enter an address in the search field to search.", Toast.LENGTH_SHORT).show();
            return;
        }

        App app = App.getAppInstance();
        FallbackGeocoder geocoder = new FallbackGeocoder(getActivity(), app.getCurrentInstance().getExtent());

        LatLng pos = geocoder.androidGeocode(address);

        if (pos == null) {
            geocoder.httpGeocode(address, handleGoogleGeocodeResponse);
        } else {
            moveMapAndFinishGeocode(pos);
         }
    }

    public void setUpBasemapControls() {
        // Create the segmented buttons
        SegmentedButton buttons = (SegmentedButton)getActivity().findViewById(R.id.basemap_controls);
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
        locationManager = (LocationManager) applicationContext.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
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
                    Bundle extras) {    }
        };
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2 * 60 * 1000, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2 * 60 * 1000, 0, locationListener);
        }
    }

    private void disableLocationUpdating() {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
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

    private void bindActionToLocationSearchBar(final View view) {
        EditText et = (EditText) view.findViewById(R.id.locationSearchField);
        et.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    doLocationSearch();
                    return true;
                } else {
                    return false;
                }
            }
        });
    }
}
