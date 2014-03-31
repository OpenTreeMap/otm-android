package org.azavea.map;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.azavea.otm.App;
import org.azavea.otm.InstanceInfo;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.UrlTileProvider;

public class TMSTileProvider extends UrlTileProvider {
	private final static int tileHeight = 256;
	private final static int tileWidth = 256;

	// Url to the Tile Server
	private String baseUrl; // http://example.com/tile/
	private String featureName;
	
	//  OTM2 specific tile requests are in the format of: 
	//    {georev}/database/otm/table/{feature}/{z}/{x}/{y}.png
	private static final String TILE_FORMAT = "%s/database/otm/table/%s/%d/%d/%d.png";

	private JSONObject parameters = new JSONObject();
	
	public TMSTileProvider(String baseUrl, String featureName) 
			throws MalformedURLException {
		super(tileWidth, tileHeight);
		this.featureName = featureName;
		this.baseUrl = new URL(baseUrl).toExternalForm();
	}

	private String formatTileUrl(int zoom, int x, int y) {
		InstanceInfo instance = App.getAppInstance().getCurrentInstance();
		String url =  baseUrl + String.format(TILE_FORMAT, instance.getGeoRevId(), 
				this.featureName, zoom, x, y);

		url += "?instance_id=" + instance.getInstanceId();
		String filters = this.parameters.toString().replace("\\", "");
		try {
			url += "&q=" + URLEncoder.encode(filters, "utf-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("TILER-TMS", url);
		return url;
	}

	public void setParameter(String key, String value) {
        try {
			this.parameters.put(key, value);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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

	@Override
	public URL getTileUrl(int x, int y, int zoom) {
        URL url = null;
        try {
            url = new URL(formatTileUrl(zoom, x, y));
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        return url;
    }
}
