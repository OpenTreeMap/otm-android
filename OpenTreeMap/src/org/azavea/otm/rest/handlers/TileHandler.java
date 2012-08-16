package org.azavea.otm.rest.handlers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.loopj.android.http.BinaryHttpResponseHandler;

public class TileHandler extends BinaryHttpResponseHandler {
	private int x;
	private int y;
	
	public TileHandler() {
		super();
	}
	
	public TileHandler(int x, int y) {
		this();
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void onSuccess(byte[] arg0) {
		super.onSuccess(arg0);
		
		Bitmap image = BitmapFactory.decodeByteArray(arg0, 0, arg0.length);
		
		Log.d("Async", "Processing (" + x + "," + y + ")");
		tileImageReceived(x, y, image);
	}
	
	@Override
	public void onFailure(Throwable arg0) {
		// TODO Auto-generated method stub
		super.onFailure(arg0);
		Log.d("Async", "Fail: " + arg0.getMessage());
		Log.d("Async", "Could not get tile (" + x + "," + y + ")");
	}
	
	@Override
	public void onFailure(Throwable arg0, String arg1) {
		// TODO Auto-generated method stub
		super.onFailure(arg0, arg1);
		Log.d("Async", "Fail(str): " + arg1);
	}
	
	// To be overriden
	public void tileImageReceived(int x, int y, Bitmap image) {
	}
}
