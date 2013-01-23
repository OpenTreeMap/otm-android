package org.azavea.otm.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.azavea.map.TMSTileProvider;
import org.azavea.otm.App;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.UrlTileProvider;


public class TileCacheTileProvider extends TMSTileProvider {

	private String baseUrl = "";
	
	public TileCacheTileProvider(int xsize, int ysize) {
		super(xsize, ysize);
		SharedPreferences prefs = App.getSharedPreferences();
		baseUrl = prefs.getString("tms_url", "");
	}

	@Override
	public String getBaseUrl() {
		return baseUrl;
	}
}

