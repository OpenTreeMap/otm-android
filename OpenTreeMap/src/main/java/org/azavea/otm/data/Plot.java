package org.azavea.otm.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.azavea.helpers.JSONHelper;
import org.azavea.otm.App;
import org.azavea.otm.rest.RequestGenerator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.common.base.Joiner;
import com.loopj.android.http.BinaryHttpResponseHandler;

import static com.google.common.collect.Collections2.filter;
import static com.google.common.collect.Lists.newArrayList;

public class Plot extends Model {

    public static final String PLOT = "plot";
    public static final String ID = "id";
    public static final String TITLE = "title";
    public static final String ADDRESS_STREET = "address_street";
    public static final String ADDRESS_CITY = "address_city";
    public static final String ADDRESS_ZIP = "address_zip";
    public static final String TREE = "tree";
    public static final String HAS_TREE = "has_tree";
    public static final String GEOM = "geom";
    public static final String PHOTOS = "photos";
    public static final String PHOTO_IMAGE = "image";
    public static final String PHOTO_THUMBNAIL = "thumbnail";

    private PendingStatus hasPending = PendingStatus.Unset;
    private JSONObject plotDetails = null;
    private Species species = null;

    enum PendingStatus {
        Pending, NoPending, Unset
    }

    /**
     * When Requesting a plot tree photo, these are the valid image types
     */
    public static String[] IMAGE_TYPES = new String[]{
            "image/jpeg", "image/png", "image/gif"
    };

    public Plot() {
        try {
            // Basic empty plot json structure
            JSONObject fullPlot = new JSONObject();
            plotDetails = new JSONObject();
            fullPlot.put(PLOT, plotDetails);
            this.setData(fullPlot);

        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Error creating empty plot", e);
        }
    }

    public Plot(JSONObject data) {
        setData(data);
    }

    @Override
    public void setData(JSONObject data) {
        super.setData(data);
        setupPlotDetails();
    }

    @Override
    public void setValueForKey(String key, Object value) throws Exception {
        // Make a tree if this key is for a tree and this plot doesn't have a tree
        if (key.split("[.]")[0].equals("tree") && !hasTree() && !JSONObject.NULL.equals(value)) {
            this.createTree();
        }
        super.setValueForKey(key, value);
    }

    private void setupPlotDetails() {
        try {
            this.plotDetails = this.data.optJSONObject(PLOT);
            if (!JSONObject.NULL.equals(plotDetails) && this.hasTree()) {
                Tree tree = this.getTree();
                JSONObject speciesData = tree.getSpecies();
                if (speciesData != null) {
                    this.species = new Species();
                    species.setData(speciesData);
                }
            }
        } catch (JSONException e) {
            this.plotDetails = null;
        }
    }

    public int getId() throws JSONException {
        return plotDetails.getInt(ID);
    }

    public String getTitle() {
        return this.data.optString(TITLE, null);
    }

    public String getAddress() {
        final String streetAddress = JSONHelper.safeGetString(plotDetails, ADDRESS_STREET);
        final String city = JSONHelper.safeGetString(plotDetails, ADDRESS_CITY);
        final String zip = JSONHelper.safeGetString(plotDetails, ADDRESS_ZIP);

        Collection<String> addresses = filter(newArrayList(streetAddress, city, zip), s -> s != null);

        return Joiner.on(", ").join(addresses);
    }

    public void setAddressFromGeocoder(Geocoder geocoder) {
        Geometry geom = getGeometry();
        if (geom == null) {
            Log.e(App.LOG_TAG, "Cannot set Address for a plot with no geometry");
            setAddressFields(null, null, null);
            return;
        }

        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(geom.getX(), geom.getY(), 1);
        } catch (Exception e) {
            Log.w(App.LOG_TAG, "Error Geocoding address", e);
            setAddressFields(null, null, null);
            return;
        }

        if ((addresses != null) && (addresses.size() != 0)) {
            Address addressData = addresses.get(0);
            String streetAddress = addressData.getMaxAddressLineIndex() == 0
                    ? null : addressData.getAddressLine(0);

            setAddressFields(streetAddress, addressData.getLocality(), addressData.getPostalCode());
        }
    }

    private void setAddressFields(String streetAddress, String city, String zip) {
        try {
            plotDetails.put(ADDRESS_CITY, city);
            plotDetails.put(ADDRESS_STREET, streetAddress);
            plotDetails.put(ADDRESS_ZIP, zip);
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Error saving geocoded address", e);
        }
    }

    public String getLastUpdated() throws JSONException {
        return data.getJSONObject("latest_update").getString("created");
    }

    public void setLastUpdated(String lastUpdated) throws JSONException {
        data.put("last_updated", lastUpdated);
    }

    public String getLastUpdatedBy() throws JSONException {
        JSONObject lastUser = data.getJSONArray("recent_activity").getJSONObject(0);
        if (lastUser != null) {
            return lastUser.getString("username");
        }
        return "";
    }

    public void setLastUpdatedBy(String lastUpdatedBy) throws JSONException {
        data.put("last_updated_by", lastUpdatedBy);
    }

    public Tree getTree() throws JSONException {
        if (data.isNull(TREE)) {
            return null;
        }
        Tree retTree = new Tree(this);
        retTree.setData(data.getJSONObject(TREE));
        return retTree;
    }

    public void setTree(Tree tree) throws JSONException {
        data.put(TREE, tree.getData());
        data.put(HAS_TREE, true);
    }

    public Geometry getGeometry() {
        if (plotDetails.isNull(GEOM)) {
            return null;
        }

        Geometry retGeom = new Geometry();
        retGeom.setData(plotDetails.optJSONObject(GEOM));
        return retGeom;
    }

    public void setGeometry(Geometry geom) throws JSONException {
        plotDetails.put(GEOM, geom.getData());
    }

    /**
     * Does this plot have current pending edits?
     *
     * @throws JSONException
     */
    public boolean hasPendingEdits() throws JSONException {
        // Use the cache if available, this might be called a lot
        if (hasPending != PendingStatus.Unset) {
            return hasPending == PendingStatus.Pending;
        }

        boolean pendings = false;
        if (!data.isNull("pending_edits")) {
            if (data.getJSONObject("pending_edits").length() > 0) {
                pendings = true;
            }
        }

        // Cache for this instance
        hasPending = pendings ? PendingStatus.Pending : PendingStatus.NoPending;
        return pendings;
    }

    /**
     * Get a pending edit description for a given field key for a plot or tree
     *
     * @param key name of field key
     * @return An object representing a pending edit description for the field,
     * or null if there are no pending edits
     * @throws JSONException
     */
    public PendingEditDescription getPendingEditForKey(String key) throws JSONException {
        if (this.hasPendingEdits()) {
            JSONObject edits = data.getJSONObject("pending_edits");
            if (!edits.isNull(key)) {
                return new PendingEditDescription(key, edits.getJSONObject(key));
            }
        }
        return null;
    }

    public boolean hasTree() {
        return data.optBoolean(HAS_TREE, false);
    }

    public void createTree() throws JSONException {
        this.setTree(new Tree());
    }

    public JSONObject getMostRecentPhoto() {
        JSONArray photos = data.optJSONArray(PHOTOS);
        if (photos != null && photos.length() > 0 && this.hasTree()) {
            List<JSONObject> photoObjects = new ArrayList<>(photos.length());
            for (int i = 0; i < photos.length(); i++) {
                JSONObject photo = photos.optJSONObject(i);
                // If we start supporting multiple trees, we'll need to check the tree id here
                if (photo != null && photo.optInt(ID) != 0 && photo.has(PHOTO_IMAGE) && photo.has(PHOTO_THUMBNAIL)) {
                    photoObjects.add(photo);
                }
            }
            return Collections.max(photoObjects, (a, b) -> a.optInt(ID) - b.optInt(ID));
        }
        return null;
    }

    /**
     * Get the most recent tree thumbnail for this plot, by way of an
     * asynchronous response handler.
     *
     * @param handler image handler which will receive callback from async http
     *                request
     */
    public void getTreeThumbnail(BinaryHttpResponseHandler handler) {
        getTreeImage(PHOTO_THUMBNAIL, handler);
    }

    public void getTreePhoto(BinaryHttpResponseHandler handler) {
        getTreeImage(PHOTO_IMAGE, handler);
    }

    private void getTreeImage(String name, BinaryHttpResponseHandler handler) {
        JSONObject photo = this.getMostRecentPhoto();
        if (photo != null) {
            String url = photo.optString(name);
            if (url != null) {
                RequestGenerator rg = new RequestGenerator();
                rg.getImage(url, handler);
            }
        }
    }

    public void assignNewTreePhoto(JSONObject image) throws JSONException {
        JSONArray photos = data.optJSONArray(PHOTOS);
        if (photos == null) {
            photos = new JSONArray();
            data.put(PHOTOS, photos);
        }
        photos.put(image);
    }

    public String getScienticName() {
        if (this.species != null) {
            return this.species.getScientificName();
        }
        return null;
    }

    public String getCommonName() {
        if (this.species != null) {
            return this.species.getCommonName();
        }
        return null;
    }

    /**
     * Get an updated georevhash from a plot update, or the current one if no
     * new one exists
     */
    public String getUpdatedGeoRev() {
        return this.data.optString("geoRevHash",
                App.getAppInstance().getCurrentInstance().getGeoRevId());
    }

    /**
     * @param pendingKey: the key to get pending edits for
     */
    public String getValueForLatestPendingEdit(String pendingKey) {
        // get the pending edit description object for this plot
        PendingEditDescription ped;
        try {
            ped = getPendingEditForKey(pendingKey);
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "JSON exception reading pending edit field", e);
            return "";
        }

        // get a list of pending edits
        List<PendingEdit> pendingEditList;
        try {
            pendingEditList = ped.getPendingEdits();
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "JSON exception reading pending edit field", e);
            return "";
        }

        // I assert that the most recent one is the first one. (argh.)
        PendingEdit mostRecentPendingEdit;
        if (pendingEditList.size() != 0) {
            mostRecentPendingEdit = pendingEditList.get(0);
        } else {
            return "";
        }

        String value;
        try {
            value = mostRecentPendingEdit.getValue();
        } catch (JSONException e) {
            value = null;
        }

        return value;

    }
}
