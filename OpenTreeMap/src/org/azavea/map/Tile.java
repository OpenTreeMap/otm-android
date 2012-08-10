package org.azavea.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Tile {
	public static final int SIZE = 160;
	public static final int HEIGHT = 800;
	public static final int WIDTH = 480;
	private GeoPoint topLeft;
	private int screenX;
	private int screenY;
	private Bitmap image;
	
	public Tile() {
		topLeft = new GeoPoint(0, 0);
	}
	
	public Tile(int lon, int lat) {
		topLeft = new GeoPoint(lat, lon);
	}
	
	public Tile(int lon, int lat, Bitmap image) {
		this(lon, lat);
		this.image = image;
	}
	
	public Tile(int x, int y, int lon, int lat, Bitmap image) {
		this(lon, lat, image);
		this.screenX = x;
		this.screenY = y;
	}
	
	public void draw(int x, int y, Canvas canvas) {
		Paint paint = new Paint();
		paint.setAlpha(0x888);
		canvas.drawBitmap(image, x, y, paint);
	}
	
	public void draw(Canvas canvas, int offsetX, int offsetY) {
		Paint paint = new Paint();
		paint.setAlpha(0x888);
		canvas.drawBitmap(image, screenX + offsetX, screenY + offsetY, paint);
	}
	
	public Bitmap getImage() {
		return image;
	}
	
	public int getX() {
		return screenX;
	}
	
	public int getY() {
		return screenY;
	}
}
