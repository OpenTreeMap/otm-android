package org.azavea.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.util.Log;

import com.google.android.gms.maps.model.UrlTileProvider;


public abstract class TMSTileProvider extends UrlTileProvider {
	
	public TMSTileProvider(int xsize, int ysize) {
		super(xsize, ysize);
	}

	public abstract String getBaseUrl();

	//TODO store the root in config
	private static String TMS_BASIC = "%s/%d/%d/%d.png";
	
	@Override
	public URL getTileUrl(int x, int y, int zoom) {
		int reversedY = (1 << zoom) - y - 1;
	    String s = String.format(Locale.US, TMS_BASIC, getBaseUrl(), zoom, x, reversedY);
        URL url = null;
        try {
            url = new URL(s);
        } catch (MalformedURLException e) {
            throw new AssertionError(e);
        }
        Log.d("TILES-TMS", url.toString());
        return url;
    }
}
