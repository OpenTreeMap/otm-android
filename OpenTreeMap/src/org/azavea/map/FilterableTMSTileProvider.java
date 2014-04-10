package org.azavea.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONObject;

import android.net.Uri;

public class FilterableTMSTileProvider extends TMSTileProvider {
    private JSONArray parameters = null;

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
        if (this.parameters != null) {
            urlBuilder.appendQueryParameter("q", this.parameters.toString());
        }

        try {
            return new URL(urlBuilder.build().toString());
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
    }

    public void setParameters(Collection<JSONObject> filters) {
        clearParameters();
        if (filters.isEmpty()) {
            return;
        }
        this.parameters = new JSONArray();
        this.parameters.put("AND");
        for (JSONObject filter : filters) {
            this.parameters.put(filter);
        }
    }

    /***
     * Remove any query string parameters from the tile requests
     */
    public void clearParameters() {
        this.parameters = null;
    }
}
