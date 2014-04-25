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
    
    public JSONObject getPlotEcoFields() {
        return getEcoFields("plot");
    }
    /**
     * Get eco definitions for a particular model type.
     * @param model ex: 'plot'
     */
    public JSONObject getEcoFields(String model) {
        JSONObject eco = this.data.optJSONObject("eco");
        if (eco != null && eco.optBoolean("supportsEcoBenefits")) {
            JSONArray benefits = eco.optJSONArray("benefits");
            if (benefits != null) {
                for (int i=0; i<benefits.length(); i++) {
                    JSONObject ecoBen = benefits.optJSONObject(i);
                    if (ecoBen != null && ecoBen.optString("model", "").equalsIgnoreCase(model)) {
                        return ecoBen;
                    }
                }
            }
        }
        return null;
    }

    public double getLat() {
        return getCenter("lat");
    }

    public double getLon() {
        return getCenter("lng");
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
