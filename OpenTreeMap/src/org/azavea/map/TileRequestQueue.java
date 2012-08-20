package org.azavea.map;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.azavea.otm.rest.handlers.TileHandler;

import android.util.Log;

// Maintains a hashtable of bounding-boxes and associated
// response handlers
public class TileRequestQueue {
	private Hashtable<GeoRect, TileHandler> tileRequests;
	
	public TileRequestQueue() {
		tileRequests = new Hashtable<GeoRect, TileHandler>();
	}
	
	public void addTileRequest(GeoRect rect, TileHandler response) {
		tileRequests.put(rect, response);
	}
	
	public void removeTileRequest(GeoRect rect) {
		if (tileRequests != null && tileRequests.containsKey(rect)) {
			tileRequests.remove(rect);
		}
	}

	public boolean containsTileRequest(GeoRect rect) {
		return tileRequests.containsKey(rect);
	}
	
	public TileHandler getTileRequestHandler(GeoRect rect) {
		if (tileRequests.containsKey(rect)) {
			return tileRequests.get(rect);
		} else {
			return null;
		}
	}
}
