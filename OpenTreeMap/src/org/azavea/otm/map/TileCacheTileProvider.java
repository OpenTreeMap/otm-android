package org.azavea.otm.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.azavea.map.TMSTileProvider;

import android.util.Log;

import com.google.android.gms.maps.model.UrlTileProvider;


public class TileCacheTileProvider extends TMSTileProvider {
	
	public TileCacheTileProvider(int xsize, int ysize) {
		super(xsize, ysize);
	}

	@Override
	public String getBaseUrl() {
		return "http://phillytreemap.org/tilecache/1.0.0/PTM";
	}
}

