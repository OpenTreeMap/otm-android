package org.azavea.map;

import java.net.URL;

import com.google.android.gms.maps.model.UrlTileProvider;

public abstract class WMSTileProvider extends UrlTileProvider {
	private static final double[] TILE_ORIGIN = {-20037508.34789244, 20037508.34789244};
    private static final int ORIG_X = 0;
    private static final int ORIG_Y = 1;
    private static final double MAP_SIZE = 20037508.34789244 * 2;
    
    // array indexes for bounding box arrays.
    protected static final int MINX = 0;
    protected static final int MAXX = 1;
    protected static final int MINY = 2;
    protected static final int MAXY = 3;
    
    public WMSTileProvider(int x, int y) {
    	super(x, y);
    }
    
	protected double[] getBoundingBox(int x, int y, int zoom) {
    	double tileSize = MAP_SIZE / Math.pow(2, zoom);
    	double minx = TILE_ORIGIN[ORIG_X] + x * tileSize;
    	double maxx = TILE_ORIGIN[ORIG_X] + (x+1) * tileSize;
    	double miny = TILE_ORIGIN[ORIG_Y] - (y+1) * tileSize;
    	double maxy = TILE_ORIGIN[ORIG_Y] - y * tileSize;
  
    	double[] bbox = new double[4];
    	bbox[MINX] = minx;
    	bbox[MINY] = miny;
    	bbox[MAXX] = maxx;
    	bbox[MAXY] = maxy;
    	
    	return bbox;
    }
	
}
