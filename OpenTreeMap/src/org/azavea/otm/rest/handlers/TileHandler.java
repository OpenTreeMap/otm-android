package org.azavea.otm.rest.handlers;

import org.azavea.map.GeoRect;
import org.azavea.map.TileRequestQueue;
import org.azavea.otm.App;
import org.azavea.otm.ui.MapDisplay;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.loopj.android.http.BinaryHttpResponseHandler;

public class TileHandler extends BinaryHttpResponseHandler {
	private int x;
	private int y;
	private boolean valid;
	private int seqId;
	private GeoRect boundingBox;
	private String url;
	
	public TileHandler() {
		super();
		valid = true;
		seqId = App.getTileRequestSeqId();
	}
	
	public TileHandler(int x, int y) {
		this();
		this.x = x;
		this.y = y;
	}
	
	public GeoRect getBoundingBox() {
		return boundingBox;
	}

	public void setBoundingBox(GeoRect boundingBox) {
		this.boundingBox = boundingBox;
	}

	public TileHandler(int x, int y, GeoRect boundingBox) {
		this(x, y);
		this.boundingBox = boundingBox;
	}
	
	@Override
	public void onSuccess(byte[] arg0) {
		super.onSuccess(arg0);
		
		Bitmap image = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
		
		// If this request is still current (i.e. hasn't been
		// superceded by a later request) and hasn't been
		// marked as scrapped, call the overridden handler
		if (valid && seqId == App.getTileRequestSeqId()) {
			if (url != null) {
				App.getTileCache().put(url, image);
			}
			tileImageReceived(x, y, image);
		}
	}
	
	@Override
	public void onFailure(Throwable arg0) {
		super.onFailure(arg0);

		Log.d(App.LOG_TAG, "Tile-load failed: " + arg0.getMessage());
		Log.d(App.LOG_TAG, "Could not get tile (" + x + "," + y + ")");
	}
	
	@Override
	public void onFailure(Throwable arg0, String arg1) {
		super.onFailure(arg0, arg1);
		Log.d(App.LOG_TAG, "Tile-load failed (Throwable, String): " + arg1);
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	
	public void scrap() {
		valid = false;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setSeqId(int seqId) {
		this.seqId = seqId;
	}
	
	// Overridden by the code making the
	// request. It is that code's job to handle
	// the response
	public void tileImageReceived(int x, int y, Bitmap image) {
	}
}
