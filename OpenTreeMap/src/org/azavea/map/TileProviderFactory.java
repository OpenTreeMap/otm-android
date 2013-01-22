package org.azavea.map;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import android.util.Log;

import com.google.android.gms.maps.model.TileProvider;

public class TileProviderFactory {
	
	private static final String GEOSERVER_OTM_FORMAT =
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
    		"&transparent=true" +
    		"&cql_filter=(%s)";	
	
	
	public static TileProvider getWmsTileProvider() {
		return getWmsCqlTileProvider("1=1");
	}

	public static TileProvider getWmsCqlTileProvider(String cql) {
		TileProvider tileProvider = new WMSTileProvider(256,256, cql) {
        	
	        @Override
	        public synchronized URL getTileUrl(int x, int y, int zoom) {
	        	double[] bbox = getBoundingBox(x, y, zoom);
	        	String cql = getCql();
	            String s = String.format(Locale.US, GEOSERVER_OTM_FORMAT, bbox[MINX], 
	            		bbox[MINY], bbox[MAXX], bbox[MAXY], cql);
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
		
	
	
}
