package org.azavea.otm;

import org.azavea.otm.data.Model;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class InstanceInfo extends Model {

    // Commonly accessed fields are loaded into class
    // fields to avoid having to deal with potential
    // JSONEncoding exceptions through the app
    private int instanceId;
    private String geoRevId;
    private String name;
    private String urlName;

    public class InstanceExtent {
        public final double minLongitude;
        public final double minLatitude;
        public final double maxLongitude;
        public final double maxLatitude;

        public InstanceExtent(double minLng, double minLat, double maxLng, double maxLat) {
            this.minLongitude = minLng;
            this.minLatitude = minLat;
            this.maxLongitude = maxLng;
            this.maxLatitude = maxLat;
        }
    }

    // Default constructor required for RestHandler instantiation
    public InstanceInfo() {
    }

    public InstanceInfo(int instanceId, String geoRevId, String name) {
        this.instanceId = instanceId;
        this.geoRevId = geoRevId;
        this.name = name;
    }

    @Override
    public void setData(JSONObject data) {
        try {
            name = data.getString("name");
            setGeoRevId(data.getString("geoRevHash"));
            urlName = data.getString("url");
            instanceId = data.getInt("id");
            super.setData(data);

        } catch (JSONException ex) {
            Log.e(App.LOG_TAG, "Invalid Instance Info Received", ex);
        }
    }

    public String getName() {
        return name;
    }

    public String getGeoRevId() {
        return geoRevId;
    }

    public void setGeoRevId(String geoRevId) {
        this.geoRevId = geoRevId;
    }

    public int getInstanceId() {
        return instanceId;
    }

    public String getUrlName() {
        return urlName;
    }

    public JSONArray getDisplayFieldKeys() {
        return (JSONArray) getField("field_key_groups");
    }

    public JSONObject getFieldDefinitions() {
        return (JSONObject) getField("fields");
    }

    public JSONObject getSearchDefinitions() {
        return (JSONObject) getField("search");
    }

    public JSONArray getEcoFields() {
        JSONObject eco = this.data.optJSONObject("eco");
        if (eco != null && eco.optBoolean("supportsEcoBenefits")) {
            JSONArray benefits = eco.optJSONArray("benefits");
            return benefits;
        }
        return null;
    }

    public double getLat() {
        return getCenter("lat");
    }

    public double getLon() {
        return getCenter("lng");
    }

    public InstanceExtent getExtent() {
        try {
            JSONObject json = data.getJSONObject("extent");

            return new InstanceExtent(json.getDouble("min_lng"), json.getDouble("max_lat"), json.getDouble("max_lng"),
                    json.getDouble("max_lat"));
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Invalid Instance extent Received", e);
        }
        return null;
    }

    private double getCenter(String coordinatePart) {
        try {
            JSONObject center = (JSONObject)getField("center");
            return center.getDouble(coordinatePart);
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Can't get center-part for instance:"
                + coordinatePart, e);
            return 0;
        }
    }
}
