package org.azavea.map;

import java.net.MalformedURLException;
import java.net.URL;

import org.azavea.otm.App;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.util.Log;

public class FilterableTMSTileProvider extends TMSTileProvider {
    private JSONObject parameters = new JSONObject();

    public FilterableTMSTileProvider(String baseUrl, String featureName) 
            throws MalformedURLException {
        super(baseUrl, featureName);
    }

    public FilterableTMSTileProvider(String baseUrl, String featureName, int opacity)
            throws MalformedURLException {
        super(baseUrl, featureName, opacity);
    }

    @Override
    public URL getTileUrl(int x, int y, int zoom) {
        URL unfilteredUrl = super.getTileUrl(x, y, zoom);
        if (unfilteredUrl == null) {
            return null;
        }

        Uri.Builder urlBuilder = Uri.parse(unfilteredUrl.toString()).buildUpon();
        urlBuilder.appendQueryParameter("q", this.parameters.toString());

        try {
            return new URL(urlBuilder.build().toString());
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public void setParameter(String key, String value) {
        try {
            this.parameters.put(key, value);
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Bad parameter", e);
        }
    }

    public void setRangeParameter(String key, String min, String max) {
        JSONObject vals = new JSONObject();
        try {
            vals.put("MIN", min);
            vals.put("MAX", max);
            this.parameters.put(key, vals);
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Bad range parameter", e);
        }
    }

    /***
     * Remove any query string parameters from the tile requests
     */
    public void clearParameters() {
        this.parameters = new JSONObject();
    }
}
