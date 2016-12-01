package org.azavea.otm.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.atlassian.fugue.Either;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.joelapenna.foursquared.widget.SegmentedButton;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;

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
import org.jdeferred.Deferred;
import org.jdeferred.DeferredManager;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.impl.DeferredObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeoutException;

import cz.msebera.android.httpclient.Header;

public class MainMapFragment extends Fragment implements GoogleApiClient.ConnectionCallbacks {
    private static final int STREET_ZOOM_LEVEL = 17;
    private static final int FILTER_INTENT = 1;
    private static final int INFO_INTENT = 2;
    private static final int ADD_INTENT = 3;

    // modes for the add tree marker feature
    private static final int STEP1 = 1;
    private static final int STEP2 = 2;
    private static final int CANCEL = 3;
    private static final int STEP3 = 4;

    private Menu menu;
    private SearchView searchView;
    private TextView plotSpeciesView;
    private TextView plotAddressView;
    private ImageView plotImageView;
    private RelativeLayout plotPopup;
    private Plot currentPlot; // The Plot we're currently showing a pop-up for, if any
    private Marker plotMarker;
    private MapView mapView;
    private TextView filterDisplay;
    private int treeAddMode = CANCEL;
    private GoogleApiClient mGoogleApiClient;

    private Deferred<Location, Throwable, Void> mLocationDeferred;

    // the map setup can last the entire life of the fragment instance
    private Deferred<GoogleMap, Throwable, Void> mMapSetupDeferred = new DeferredObject<>();

    private Promise<InstanceInfo, Throwable, Void> mInstanceLoadPromise;

    FilterableTMSTileProvider filterTileProvider;
    TMSTileProvider boundaryTileProvider;
    TMSTileProvider canopyTileProvider;
    TileOverlay filterTileOverlay;
    TileOverlay canopyTileOverlay;
    TileOverlay boundaryTileOverlay;

    public void onBackPressed() {
        hidePopup();
        removePlotMarker();
        mMapSetupDeferred.promise().done(map -> setTreeAddMode(CANCEL, map));
        searchView.setIconified(true);
    }

    public boolean shouldHandleBackPress() {
        return treeAddMode != CANCEL || currentPlot != null || !searchView.isIconified();
    }

    /**
     * ****************************************************
     * Overrides for the Fragment base class
     * *****************************************************
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.main_map, container, false);
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
            mMapSetupDeferred.promise().done(this::handleAddTree);
            return true;
        } else {
            return false;
        }
    }

    // Called after onCreateView, so getView() is guaranteed to be non-null.
    //
    // Unlike activities, only our own app is responsible for fragment life cycle.
    // We never detach this fragment from an activity and later reattach it,
    // so onActivityCreated only gets called once in the life cycle of the fragment.
    //
    // If we ever do start detaching and re-attaching it, or re-intenting its parent `TabLayout`,
    // more guards must be put in place.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In the highly unlikely event that tile provider urls are malformed, fail fast.
        if (!setupTileProviders(mMapSetupDeferred)) {
            return;
        }

        Deferred<InstanceInfo, Throwable, Void> instanceLoadDeferred = new DeferredObject<>();
        mInstanceLoadPromise = instanceLoadDeferred.promise();
        mLocationDeferred = new DeferredObject<>();
        final Promise<Location, Throwable, Void> locationPromise = mLocationDeferred.promise();
        final Promise<GoogleMap, Throwable, Void> mapSetupPromise = mMapSetupDeferred.promise();

        final DeferredManager dm = new AndroidDeferredManager();

        final int INSTANCE = 0;
        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                "Loading Map Info...", true);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .build();
        }

        App.getAppInstance().ensureInstanceLoaded(result -> {
            if (result != null && result.getData().getBoolean("success")) {
                instanceLoadDeferred.resolve(App.getCurrentInstance());
            } else {
                instanceLoadDeferred.reject(new Throwable("Instance load failed"));
                return false;
            }
            return true;
        });

        // Functionality dependent on the GoogleMap but not on the treeMap InstanceInfo.
        mapSetupPromise.then(map -> {
            setUpMap(getView(), map);
            setupControls();
        });
        // Functionality dependent on both.
        dm.when(mInstanceLoadPromise, mapSetupPromise).then(results -> {
            // Will execute immediately, since we got here thanks to the promise being resolved.
            // Simply a way of retrieving a correctly typed map.
            mapSetupPromise.done(map -> {
                setupViewHandlers(getView(), map);
                setProviderDisplayParameters();
                InstanceInfo treeMap = InstanceInfo.class.cast(results.get(INSTANCE).getResult());
                initLocation(treeMap, locationPromise, map).always((state, resolved, rejected) ->
                        dialog.dismiss());
            });
        }, failure -> {
            dialog.dismiss();
            Throwable e = Throwable.class.cast(failure.getReject());
            Logger.error("Unable to setup map", e);
            Toast.makeText(App.getAppInstance(), R.string.map_failed, Toast.LENGTH_SHORT).show();
        });

        // Since Google Play services is required in order to obtain a GoogleMap,
        // check whether it is correctly installed and give the user a chance to rectify it.
        //
        // If it is not, the MapHelper will show a dialog which will redirect to an activity in
        // another app (play store or system settings) to rectify the situation.
        // A user can return to this Activity and Fragment after following the prompt and correctly
        // installing/updating/enabling the Google Play services.
        // The MapHelper does not tell whether the situation has been rectified,
        // so a future attempt to fetch a GoogleMap still may fail.
        Activity activity = getActivity();
        MapsInitializer.initialize(activity);
        MapHelper.checkGooglePlay(activity);

        mapView = (MapView) getView().findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);

        // If the above MapHelper check did not rectify an incorrect Google Play installation,
        // the same prompt may be shown during getMapAsync.
        mapView.getMapAsync(map -> {
            if (map != null) {
                mMapSetupDeferred.resolve(map);
            } else {
                Logger.warning("Map was null, missing google play support?");
                mMapSetupDeferred.reject(new Exception("Map was null, missing google play support?"));
            }
        });
    }

    // Inevitably called after onActivityCreated. Means the fragment is visible.
    @Override
    public void onStart() {
        super.onStart();

        // Can happen in the extremely unlikely event that tile providers setup failed,
        // or in the equally unlikely event that Google Play services is not setup correctly
        // and the user failed to fix it when prompted by checkGooglePlay.
        if (mMapSetupDeferred.promise().isRejected()) {
            return;
        }
        // mInstanceLoadPromise could be rejected if ensureInstanceLoaded failed fast
        if (mInstanceLoadPromise.isRejected()) {
            return;
        }

        mapView.onStart();

        // Even though the deferred is only resolved once,
        // a done handler gets called every time .done is called thereafter.
        mMapSetupDeferred.promise().done(map -> map.setMyLocationEnabled(true));

        // Should result in onConnect being called.
        mGoogleApiClient.connect();
    }

    // Usually called after onStart. Means the fragment got the focus.
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();

        // Even though the deferred is only resolved once,
        // a done handler gets called every time .done is called thereafter.
        mMapSetupDeferred.promise().done(map -> {
            map.setMyLocationEnabled(false);
        });

        mapView.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    // Shown when the user has added a tree, set a filter, or clicked on a tree
    // in response to startActivityForResult
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mMapSetupDeferred.promise().done(map -> {
            switch (requestCode) {
                case FILTER_INTENT:
                    if (resultCode == Activity.RESULT_OK) {
                        Collection<Either<JSONObject, JSONArray>> activeFilters = App.getFilterManager().getActiveFilters();
                        setFilterDisplay(App.getFilterManager().getActiveFilterDisplay());

                        filterTileProvider.setParameters(activeFilters);
                        reloadTiles(map);
                    }
                    break;
                case INFO_INTENT:
                    if (resultCode == TreeDisplay.RESULT_PLOT_EDITED) {
                        showPlotFromIntent(data, map);
                    } else if (resultCode == TreeDisplay.RESULT_PLOT_DELETED) {
                        hidePopup();
                        reloadTiles(map);
                        // TODO: Do we need to refresh the map tile?
                    }
                    break;

                case ADD_INTENT:
                    if (resultCode == Activity.RESULT_OK) {
                        reloadTiles(map);
                        showPlotFromIntent(data, map);
                        setTreeAddMode(CANCEL, map);
                    }
            }
        });
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

    /**
     * ***************************************************************
     * Overrides for the GoogleApiClient.ConnectionCallbacks interface
     * ***************************************************************
     */
    @Override
    public void onConnected(Bundle bundle) {
        int WAIT_DURATION = 5000;  // 5 seconds
        LocationRequest request = LocationRequest.create()
                .setNumUpdates(1)
                .setExpirationDuration(WAIT_DURATION)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, request, loc -> {
                    if (mLocationDeferred.isPending()) {
                        mLocationDeferred.resolve(loc);
                    }
                });
        new Handler().postDelayed(() -> {
            if (mLocationDeferred.isPending()) {
                mLocationDeferred.reject(new TimeoutException("Location request timed out"));
            }
        }, WAIT_DURATION);
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Implement the interface
    }

    /*********************************
     * Private methods
     *********************************/

    private void showPlotFromIntent(Intent data, GoogleMap map) {
        try {
            // The plot was updated, so update the pop-up with any new data
            String plotJSON = data.getExtras().getString("plot");
            Plot updatedPlot = new Plot(new JSONObject(plotJSON));
            showPopup(updatedPlot, map);

        } catch (JSONException e) {
            Logger.error("Unable to deserialze updated plot for map popup", e);
            hidePopup();
        }
    }

    private void showPopupOnMap(LatLng point, GoogleMap map) {
        Log.d("TREE_CLICK", "(" + point.latitude + "," + point.longitude + ")");

        final ProgressDialog dialog = ProgressDialog.show(getActivity(), "",
                "Loading. Please wait...", true);
        dialog.show();

        new RequestGenerator().getPlotsNearLocation(
                point.latitude,
                point.longitude,
                null,
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
                                Log.d("TREE_CLICK", "plot: " + plot.getTitle());
                                showPopup(plot, map);
                            } else {
                                Log.d("TREE_CLICK", "null plot");
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
    }

    private boolean setupTileProviders(Deferred<GoogleMap, Throwable, Void> mapDeferred) {
        final SharedPreferences prefs = App.getSharedPreferences();
        final String baseTileUrl = prefs.getString("tiler_url", null);
        final String boundaryFeature = prefs.getString("boundary_feature", null);
        final String plotFeature = prefs.getString("plot_feature", null);

        try {
            boundaryTileProvider = new TMSTileProvider(baseTileUrl, boundaryFeature);
            canopyTileProvider = new TMSTileProvider(baseTileUrl, plotFeature);
            if (filterTileProvider == null) {
                filterTileProvider = new FilterableTMSTileProvider(baseTileUrl, plotFeature);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Logger.error("Unable to setup map tile provider, baseTileUrl: " + baseTileUrl, e);
            Toast.makeText(App.getAppInstance(), R.string.map_failed, Toast.LENGTH_SHORT).show();
            mapDeferred.reject(e);

            return false;
        }
        return true;
    }

    // Sets up the filterDisplay, plotPopup, and plot related views.
    // Called from onActivityCreated, so getView() is guaranteed to return non-null.
    private void setupControls() {
        final View view = getView();
        filterDisplay = (TextView) view.findViewById(R.id.filterDisplay);
        plotPopup = (RelativeLayout) view.findViewById(R.id.plotPopup);
        plotSpeciesView = (TextView) view.findViewById(R.id.plotSpecies);
        plotAddressView = (TextView) view.findViewById(R.id.plotAddress);
        plotImageView = (ImageView) view.findViewById(R.id.plotImage);
    }

    private void setProviderDisplayParameters() {
        String[] displayFilters = App.getAppInstance().getResources().getStringArray(R.array.display_filters);
        canopyTileProvider.setDisplayParameters(Arrays.asList(displayFilters));
        filterTileProvider.setDisplayParameters(Arrays.asList(displayFilters));
    }

    private void reloadTiles(GoogleMap map) {
        map.clear();
        setupMapOverlays(map);
        setupCanopyOverlay(map);
    }

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

    private void setUpMap(View view, GoogleMap map) {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(false);
        map.setOnMarkerDragListener(new GoogleMapsListeners.NoopDragListener());

        setupMapOverlays(map);

        // Set up the default click listener
        map.setOnMapClickListener(point -> {
            showPopupOnMap(point, map);
        });
        SegmentedButton buttons = (SegmentedButton) view.findViewById(R.id.basemap_controls);

        MapHelper.setUpBasemapControls(buttons, map);
    }

    private void setupMapOverlays(GoogleMap map) {
        try {
            boundaryTileOverlay = map.addTileOverlay(
                    new TileOverlayOptions().tileProvider(boundaryTileProvider).zIndex(0));

            filterTileOverlay = map.addTileOverlay(new TileOverlayOptions().tileProvider(filterTileProvider));
        } catch (Exception e) {
            Logger.error("Error Setting Up Basemap", e);
            Toast.makeText(getActivity(), "Error Setting Up Base Map", Toast.LENGTH_LONG).show();
        }
    }

    private void setupCanopyOverlay(GoogleMap map) {
        try {
            // Canopy layer shows all trees, is always on, but is 'dimmed' while a filter is active
            canopyTileOverlay = map.addTileOverlay(
                    new TileOverlayOptions()
                            .tileProvider(canopyTileProvider)
                            .zIndex(50)
                            .transparency(0.7f));
        } catch (Exception e) {
            Logger.error("Error setting up transparent canopy layer", e);
            Toast.makeText(getActivity(), "Error Setting Up Basemap", Toast.LENGTH_LONG).show();
        }
    }

    private Promise<Location, Throwable, Void> initLocation(
            InstanceInfo treeMap, Promise<Location, Throwable, Void> locationPromise,
            GoogleMap map) {

        SharedPreferences prefs = App.getSharedPreferences();
        int startingZoomLevel = Integer.parseInt(prefs.getString("starting_zoom_level", "12"));

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(App.getCurrentInstance().getStartPos(),
                                                         startingZoomLevel));

        locationPromise.then(loc -> {
            LatLng latlng = new LatLng(loc.getLatitude(), loc.getLongitude());
            if (treeMap.getExtent().contains(latlng)) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, STREET_ZOOM_LEVEL));
            }
        });
        return locationPromise;
    }

    private void setupViewHandlers(View view, GoogleMap map) {
        view.findViewById(R.id.plotImage).setOnClickListener(v -> {
            if (currentPlot != null) {
                currentPlot.getTreePhoto(MapHelper.getPhotoDetailHandler(getActivity(), currentPlot));
            }
        });

        view.findViewById(R.id.treeAddNext).setOnClickListener(v -> setTreeAddMode(STEP3, map));

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

    private void handleAddTree(GoogleMap map) {
        if (!App.getLoginManager().isLoggedIn()) {
            startActivity(new Intent(getActivity(), LoginActivity.class));
        } else if (!App.getCurrentInstance().canAddTree()) {
            Toast.makeText(getActivity(), getString(R.string.perms_add_tree_fail), Toast.LENGTH_SHORT).show();
        } else {
            setTreeAddMode(CANCEL, map);
            setTreeAddMode(STEP1, map);
        }
    }

    private void showPopup(Plot plot, GoogleMap map) {
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

            LatLng position = zoomToPlot(plot, map);

            removePlotMarker();
            plotMarker = map.addMarker(new MarkerOptions()
                    .position(position)
                    .title("")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker)));
        } catch (JSONException e) {
            Logger.error("Could not show tree popup", e);
        }
        currentPlot = plot;
        plotPopup.setVisibility(View.VISIBLE);
    }

    private LatLng zoomToPlot(Plot plot, GoogleMap map) throws JSONException {
        LatLng position = new LatLng(plot.getGeometry().getY(), plot.getGeometry().getX());
        if (map.getCameraPosition().zoom >= STREET_ZOOM_LEVEL) {
            map.animateCamera(CameraUpdateFactory.newLatLng(position));
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(position, STREET_ZOOM_LEVEL));
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
        if (activeFilterDisplay.equals("")) {
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
     *  STEP3 : Create tree and redirect to tree detail page.
     */
    private void setTreeAddMode(int step, GoogleMap map) {
        this.treeAddMode = step;

        View step1 = getActivity().findViewById(R.id.addTreeStep1);
        View step2 = getActivity().findViewById(R.id.addTreeStep2);
        switch (step) {
            case CANCEL:
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.GONE);
                map.setOnMapClickListener(point -> showPopupOnMap(point, map));
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

                map.setOnMapClickListener(point -> {
                    Log.d("TREE_CLICK", "(" + point.latitude + "," + point.longitude + ")");

                    plotMarker = map.addMarker(new MarkerOptions()
                            .position(point)
                            .title("New Tree")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_mapmarker))
                    );
                    plotMarker.setDraggable(true);
                    setTreeAddMode(STEP2, map);
                });
                break;
            case STEP2:
                hidePopup();
                hideMenuItems();
                step1.setVisibility(View.GONE);
                step2.setVisibility(View.VISIBLE);
                map.setOnMapClickListener(null);
                break;
            case STEP3:
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
                    setTreeAddMode(CANCEL, map);
                    Toast.makeText(getActivity(), "Error creating new tree", Toast.LENGTH_LONG).show();
                }
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
        searchView = (SearchView) menu.findItem(R.id.search_button).getActionView();
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
        mMapSetupDeferred.promise().done(map ->
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, STREET_ZOOM_LEVEL))
        );
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
    private void doLocationSearch(CharSequence query) {
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
}
