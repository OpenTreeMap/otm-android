package org.azavea.map;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.azavea.otm.App;
import org.azavea.otm.rest.handlers.TileHandler;

import com.loopj.android.http.AsyncHttpClient;

import android.graphics.Bitmap;
import android.util.Log;

public class WMSTileCache {
	private int maxSize = 100;
	private LinkedHashMap<String, Bitmap> tileImages;
	private AsyncHttpClient client;
	
	public WMSTileCache(AsyncHttpClient client) {
		this.client = client;
		tileImages = new LinkedHashMap<String, Bitmap>(maxSize*4/3, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
				return size() > maxSize;
			}
		};
	}
	
	public WMSTileCache(AsyncHttpClient client, int maximumSize) {
		this.client = client;
		this.maxSize = maximumSize;
		tileImages = new LinkedHashMap<String, Bitmap>(maxSize*4/3, 0.75f, true) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<String, Bitmap> eldest) {
				return size() > maxSize;
			}
		};
	}
	
	public void get(String url, TileHandler responseHandler) {
		if (tileImages.containsKey(url)) {
			Log.d(App.LOG_TAG, "Cache hit");

			responseHandler.onSuccess(convertBitmapToBytes(tileImages.get(url)));
		} else {
			Log.d(App.LOG_TAG, "Cache miss");
			
			// Inject URL into handler - handler will then add its image
			// to the cache
			responseHandler.setUrl(url);
			miss(url, responseHandler);
		}
	}
	
	public void put(String url, Bitmap image) {
		if (!tileImages.containsKey(url)) {
			tileImages.put(url, image);			
		}
		Log.d(App.LOG_TAG, "Cache size = " + tileImages.size());
	}
	
	// What to do if we don't have the requested tile
	private void miss(String url, TileHandler responseHandler) {
		client.get(url, responseHandler);
	}
	
	private byte[] convertBitmapToBytes(Bitmap image) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}
}
