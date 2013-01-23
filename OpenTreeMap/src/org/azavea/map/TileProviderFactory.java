package org.azavea.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;

import android.util.Log;

import com.google.android.gms.maps.model.TileProvider;

public class TileProviderFactory {
	
	private static final String GEOSERVER_OTM_BASIC =
    		"http://phillytreemap.org/geoserver/wms" +
    		"?service=WMS" +
    		"&version=1.1.1" +  			
    		"&request=GetMap" +
    		"&layers=ptm" +
    		"&bbox=%f,%f,%f,%f" +
    		"&width=256" +
    		"&height=256" +
    		"&srs=EPSG:900913" +
    		"&format=image/png" +				
    		"&transparent=true";	

	private static final String GEOSERVER_OTM_FILTERABLE =
    		"http://phillytreemap.org/geoserver/wms" +
    		"?service=WMS" +
    		"&version=1.1.1" +  			
    		"&request=GetMap" +
    		"&layers=tree_search" +
    		"&bbox=%f,%f,%f,%f" +
    		"&width=256" +
    		"&height=256" +
    		"&srs=EPSG:900913" +
    		"&format=image/png" +				
    		"&transparent=true" +
    		"&cql_filter=%s" +
    		"&styles=tree_highlight";	

	
	public static WMSTileProvider getWmsTileProvider() {
		WMSTileProvider tileProvider = new WMSTileProvider(256,256) {
        	
	        @Override
	        public synchronized URL getTileUrl(int x, int y, int zoom) {
	        	double[] bbox = getBoundingBox(x, y, zoom);
	            String s = String.format(Locale.US, GEOSERVER_OTM_BASIC, bbox[MINX], 
	            		bbox[MINY], bbox[MAXX], bbox[MAXY]);
	            Log.d("TILES", s);
	            URL url = null;
	            try {
	                url = new URL(s);
	            } catch (MalformedURLException e) {
	                throw new AssertionError(e);
	            }
	            return url;
	        }
		};
		return tileProvider;
	}

	public static WMSTileProvider getWmsCqlTileProvider() {
		WMSTileProvider tileProvider = new WMSTileProvider(256,256) {
        	
	        @Override
	        public synchronized URL getTileUrl(int x, int y, int zoom) {
	        	double[] bbox = getBoundingBox(x, y, zoom);
	        	String cql = getCql();
	        	
	        	String s = String.format(Locale.US, GEOSERVER_OTM_FILTERABLE, bbox[MINX], 
	            		bbox[MINY], bbox[MAXX], bbox[MAXY], cql);
	            URL url = null;
	            try {
	                url = new URL(s);
	            } catch (MalformedURLException e) {
	                throw new AssertionError(e);
	            }
	            Log.d("TILES-FILTER", url.toString());
	            return url;
	        }
	       
		};
		return tileProvider;
	}
		
	
	
}
