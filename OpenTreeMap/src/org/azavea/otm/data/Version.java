package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Version extends Model {
    protected String otmVersion;
    protected String apiVersion;

    public Version() {
        data = new JSONObject();
    }

    public Version(String otmVersion, String apiVersion) throws JSONException {
        this();
        data.put("otm_version", otmVersion);
        data.put("api_version", apiVersion);
    }

    public String getOtmVersion() throws JSONException {
        return data.getString("otm_version");
    }

    public String getApiVersion() throws JSONException {
        return data.getString("api_version");
    }

    public void setOtmVersion(String otmVersion) throws JSONException {
        data.put("otm_version", otmVersion);
    }

    public void setApiVersion(String apiVersion) throws JSONException {
        data.put("api_version", apiVersion);
    }
}
