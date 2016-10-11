package org.azavea.otm.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.atlassian.fugue.Either;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.joelapenna.foursquared.widget.SegmentedButton;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.azavea.helpers.GoogleMapsListeners;
import org.azavea.helpers.Logger;
import org.azavea.map.FilterableTMSTileProvider;
import org.azavea.map.TMSTileProvider;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Geometry;
import org.azavea.otm.data.InstanceInfo;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.PlotContainer;
import org.azavea.otm.map.FallbackGeocoder;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.azavea.otm.rest.handlers.LoggingJsonHttpResponseHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;

public class MainMapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {
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

    private Menu menu;
    private SearchView searchView;
    private TextView plotSpeciesView;
    private TextView plotAddressView;
    private ImageView plotImageView;
    private RelativeLayout plotPopup;
    private Plot currentPlot; // The Plot we're currently showing a pop-up for, if any
    private Marker plotMarker;
    private MapView mapView;
    private GoogleMap mMap;
    private TextView filterDisplay;
    private int treeAddMode = CANCEL;
    private GoogleApiClient mGoogleApiClient;

    FilterableTMSTileProvider filterTileProvider;
    TileOverlay filterTileOverlay;
    TileOverlay canopyTileOverlay;
    TileOverlay boundaryTileOverlay;

    // Map click listener for normal view mode
    private final OnMapClickListener showPopupMapClickListener = point -> {
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
                    public void failure(Throwable e, String message) {
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
                            Logger.error("Error retrieving plot info on map touch event: ", e);
                        } finally {
                            dialog.hide();
                        }
                    }
                }
        );
    };

    // Map click listener that allows us to add a tree
    private final OnMapClickListener addMarkerMapClickListener = new GoogleMap.OnMapClickListener() {
        @Override
        public void onMapClick(LatLng point) {
            Log.d("TREE_CLICK", "(" + point.latitude + "," + point.longitude + ")");

            plotMarker = mMap.addMarker(new MarkerOptions()
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
        searchView.setIconified(true);
    }

    public boolean shouldHandleBackPress() {
        return treeAddMode != CANCEL || currentPlot != null || !searchView.isIconified();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden && mMap != null) {
            mMap.setMyLocationEnabled(false);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
        }
    }

    /**
     * ****************************************************
     * Overrides for the Fragment base class
     * *****************************************************
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.main_map, container, false);

        MapsInitializer.initialize(getActivity());
        MapHelper.checkGooglePlay(getActivity());

        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                "Loading Map Info...", true);

        Callback instanceLoaded = result -> {
            try {
                if (result.getData().getBoolean("success")) {
                    START_POS = App.getCurrentInstance().getStartPos();
                    filterDisplay = (TextView) view.findViewById(R.id.filterDisplay);
                    setUpMapIfNeeded(view);
                    plotPopup = (RelativeLayout) view.findViewById(R.id.plotPopup);
                    setPopupViews(view);
                    clearTileCache();
                    setupViewHandlers(view);
                }
                return true;

            } catch (Exception e) {
                Logger.error("Unable to setup map", e);
                Toast.makeText(App.getAppInstance(), "Unable to setup map", Toast.LENGTH_LONG).show();
                return false;

            } finally {
                dialog.dismiss();
            }
        };
        // Check for an instance before loading the map
        App.getAppInstance().ensureInstanceLoaded(instanceLoaded);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.menu = menu;
        inflater.inflate(R.menu.main_menu, menu);
        setupSearchView(menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.filterButton) {
            Intent filter = new Intent(getActivity(), FilterDisplay.class);
            startActivityForResult(filter, FILTER_INTENT);
            return true;
        } else if (id == R.id.addTreeButton) {
            handleAddTree();
            return true;
        } else {
            return false;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        MapHelper.checkGooglePlay(getActivity());
        mapView.onResume();
        if (App.getAppInstance().getCurrentInstance() != null) {
            setUpMapIfNeeded(getView());
            setTreeAddMode(CANCEL);
            clearTileCache();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case FILTER_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    Collection<Either<JSONObject, JSONArray>> activeFilters = App.getFilterManager().getActiveFilters();
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
            Logger.error("Unable to deserialze updated plot for map popup", e);
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

    private void hideMenuItems() {
        if (menu != null) {
            menu.setGroupVisible(R.id.main_map_menu_group, false);
        }
    }

    private void showMenuItems() {
        if (menu != null) {
            menu.setGroupVisible(R.id.main_map_menu_group, true);
        }
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap(View)} once when {@link #mMap} is not null.
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
    private void setUpMapIfNeeded(View view) {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the MapView.
            mMap = mapView.getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap(view);
            } else {
                Toast.makeText(getActivity(), "Google Play store support is required to run this app.", Toast.LENGTH_LONG).show();
                Logger.warning("Map was null, missing google play support?");
            }
        }
    }

    private void setUpMap(View view) {
        SharedPreferences prefs = App.getSharedPreferences();
        int startingZoomLevel = Integer.parseInt(prefs.getString("starting_zoom_level", "12"));
        String baseTileUrl = prefs.getString("tiler_url", null);
        String plotFeature = prefs.getString("plot_feature", null);
        String boundaryFeature = prefs.getString("boundary_feature", null);
        String[] displayFilters = App.getAppInstance().getResources().getStringArray(R.array.display_filters);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(START_POS, startingZoomLevel));
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.setOnMarkerDragListener(new GoogleMapsListeners.NoopDragListener());

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
            SegmentedButton buttons = (SegmentedButton) view.findViewById(R.id.basemap_controls);

            MapHelper.setUpBasemapControls(buttons, mMap);
        } catch (Exception e) {
            Logger.error("Error Setting Up Basemap", e);
            Toast.makeText(getActivity(), "Error Setting Up Basemap", Toast.LENGTH_LONG).show();
        }
    }

    private void moveToCurrentLocation() {
        LocationRequest request = LocationRequest.create()
                .setNumUpdates(1)
                .setExpirationDuration(5000)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, request, loc -> {
                    LatLng latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
                    App.getAppInstance().ensureInstanceLoaded(result -> {
                        LatLngBounds extent = App.getCurrentInstance().getExtent();
                        if (extent.contains(latlng)) {
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(latlng, STREET_ZOOM_LEVEL));
                        }
                        return false;
                    });

                });
    }

    private void setupViewHandlers(View view) {
        view.findViewById(R.id.plotImage).setOnClickListener(v -> {
            if (currentPlot != null) {
                currentPlot.getTreePhoto(MapHelper.getPhotoDetailHandler(getActivity(), currentPlot));
            }
        });

        view.findViewById(R.id.treeAddNext).setOnClickListener(v -> setTreeAddMode(FINISH));

        view.findViewById(R.id.plotPopup).setOnClickListener(v -> {
            // Show TreeInfoDisplay with current plot
            Intent viewPlot = new Intent(getActivity(), TreeInfoDisplay.class);
            viewPlot.putExtra("plot", currentPlot.getData().toString());

            if (App.getLoginManager().isLoggedIn()) {
                viewPlot.putExtra("user", App.getLoginManager().loggedInUser.getData().toString());
            }
            startActivityForResult(viewPlot, INFO_INTENT);
        });
    }

    private void handleAddTree() {
        if (!App.getLoginManager().isLoggedIn()) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        } else if (!App.getCurrentInstance().canAddTree()) {
            Toast.makeText(getActivity(), getString(R.string.perms_add_tree_fail), Toast.LENGTH_SHORT).show();
        } else {
            setTreeAddMode(CANCEL);
            setTreeAddMode(STEP1);
        }
    }

    private void showPopup(Plot plot) {
        //set default text
        plotSpeciesView.setText(getString(R.string.species_missing));
        plotAddressView.setText(getString(R.string.address_missing));
        plotImageView.setImageResource(R.drawable.missing_tree_photo);

        try {
            String addr = plot.getAddress();
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
            Logger.error("Could not show tree popup", e);
        }
        currentPlot = plot;
        plotPopup.setVisibility(View.VISIBLE);
    }

    private LatLng zoomToPlot(Plot plot) throws JSONException {
        LatLng position = new LatLng(plot.getGeometry().getY(), plot.getGeometry().getX());
        if (mMap.getCameraPosition().zoom >= STREET_ZOOM_LEVEL) {
            mMap.animateCamera(CameraUpdateFactory.newLatLng(position));
        } else {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, STREET_ZOOM_LEVEL));
        }
        return position;
    }

    private void hidePopup() {
        RelativeLayout plotPopup = (RelativeLayout) getActivity().findViewById(R.id.plotPopup);
        plotPopup.setVisibility(View.INVISIBLE);
        currentPlot = null;
    }

    private void removePlotMarker() {
        if (plotMarker != null) {
            plotMarker.remove();
            plotMarker = null;
        }
    }

    private void setPopupViews(View view) {
        plotSpeciesView = (TextView) view.findViewById(R.id.plotSpecies);
        plotAddressView = (TextView) view.findViewById(R.id.plotAddress);
        plotImageView = (ImageView) view.findViewById(R.id.plotImage);
    }

    private void showImageOnPlotPopup(Plot plot) {
        plot.getTreeThumbnail(new BinaryHttpResponseHandler(Plot.IMAGE_TYPES) {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] imageData) {
                ImageView plotImage = (ImageView) getActivity().findViewById(R.id.plotImage);
                plotImage.setImageBitmap(BitmapFactory.decodeByteArray(imageData, 0, imageData.length));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] imageData, Throwable e) {
                // Log the error, but not important enough to bother the user
                Logger.warning("Could not retreive tree image", e);
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

    /* tree add modes:
     *  CANCEL : not adding a tree
     *  STEP1  : "Tap to add a tree"
     *  STEP2  : "Long press to move the tree into position, then click next"
     *  FINISH : Create tree and redirect to tree detail page.
     */
    public void setTreeAddMode(int step) {
        if (mMap == null) {
            return;
        }
        this.treeAddMode = step;

        View step1 = getActivity().findViewById(R.id.addTreeStep1);
        View step2 = getActivity().findViewById(R.id.addTreeStep2);
        switch (step) {
            case CANCEL:
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.GONE);
                mMap.setOnMapClickListener(showPopupMapClickListener);
                showMenuItems();
                break;
            case STEP1:
                hidePopup();
                removePlotMarker();
                hideMenuItems();
                step2.setVisibility(View.GONE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
                    step1.setTranslationY(step1.getHeight());
                    step1.setVisibility(View.VISIBLE);
                    step1.animate().translationY(0);
                } else {
                    step1.setVisibility(View.VISIBLE);
                }

                if (mMap != null) {
                    mMap.setOnMapClickListener(addMarkerMapClickListener);
                }
                break;
            case STEP2:
                hidePopup();
                hideMenuItems();
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.VISIBLE);
                if (mMap != null) {
                    mMap.setOnMapClickListener(null);
                }
                break;
            case FINISH:
                Intent editPlotIntent = new Intent(getActivity(), TreeEditDisplay.class);
                Plot newPlot;
                try {
                    newPlot = getPlotForNewTree();
                    String plotString = newPlot.getData().toString();
                    editPlotIntent.putExtra("plot", plotString);
                    editPlotIntent.putExtra("new_tree", "1");
                    startActivityForResult(editPlotIntent, ADD_INTENT);

                } catch (Exception e) {
                    Logger.error("Error creating tree", e);
                    setTreeAddMode(CANCEL);
                    Toast.makeText(getActivity(), "Error creating new tree", Toast.LENGTH_LONG).show();
                }
                this.treeAddMode = CANCEL;
                showMenuItems();
        }
    }

    private Plot getPlotForNewTree() throws JSONException {
        Plot newPlot = new Plot();
        Geometry newGeometry = new Geometry();
        double lat = plotMarker.getPosition().latitude;
        double lon = plotMarker.getPosition().longitude;
        newGeometry.setY(lat);
        newGeometry.setX(lon);

        // We always get coordinates in lat/lon
        newGeometry.setSrid(4326);
        newPlot.setGeometry(newGeometry);

        newPlot.setAddressFromGeocoder(new Geocoder(getActivity(), Locale.getDefault()));

        return newPlot;
    }

    private void setupSearchView(Menu menu) {
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search_button));
        searchView.setQueryHint(getString(R.string.search_field_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                doLocationSearch(s);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void moveMapAndFinishGeocode(LatLng pos) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, STREET_ZOOM_LEVEL));
    }

    private void alertGeocodeError() {
        Toast.makeText(getActivity(), "Location search error.", Toast.LENGTH_SHORT).show();
    }

    JsonHttpResponseHandler handleGoogleGeocodeResponse = new LoggingJsonHttpResponseHandler() {
        public void onSuccess(int statusCode, Header[] headers, JSONObject data) {
            LatLng pos = FallbackGeocoder.decodeGoogleJsonResponse(data);
            if (pos == null) {
                alertGeocodeError();
            } else {
                moveMapAndFinishGeocode(pos);
            }
        }

        @Override
        public void failure(Throwable arg0, String arg1) {
            alertGeocodeError();
        }
    };

    /* Read the location search field, geocode it, and zoom to the location. */
    public void doLocationSearch(CharSequence query) {
        String address = query.toString();

        if (address.equals("")) {
            Toast.makeText(getActivity(), "Enter an address in the search field to search.", Toast.LENGTH_SHORT).show();
            return;
        }

        FallbackGeocoder geocoder = new FallbackGeocoder(getActivity(), App.getCurrentInstance());

        LatLng pos = geocoder.androidGeocode(address);

        if (pos == null) {
            geocoder.httpGeocode(address, handleGoogleGeocodeResponse);
        } else {
            moveMapAndFinishGeocode(pos);
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

    @Override
    public void onConnected(Bundle bundle) {
        moveToCurrentLocation();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Implement the interface
    }
}
