package org.azavea.map;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;

import org.azavea.otm.rest.handlers.TileHandler;

import android.util.Log;

public class TileRequestQueue {
	private Hashtable<GeoRect, TileHandler> tileRequests;
	
	public TileRequestQueue() {
		tileRequests = new Hashtable<GeoRect, TileHandler>();
	}
	
	public void addTileRequest(GeoRect rect, TileHandler response) {
		tileRequests.put(rect, response);
		Log.d("TileRequestQueue", ""+tileRequests.size());
	}
	
	public void removeTileRequest(GeoRect rect) {
		Log.d("TileRequestQueue", "Removing: " + rect.getTop() + "," + rect.getLeft());
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
