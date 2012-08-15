package org.azavea.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Tile {
	public static final int HEIGHT = 800;
	public static final int WIDTH = 480;
	private Bitmap image;
	
	public Tile() {
	}
	
	public Tile(Bitmap image) {
		this.image = image;
	}
	
	public void draw(Canvas canvas, int x, int y, int offsetX, int offsetY) {
		Paint paint = new Paint();
		paint.setAlpha(0x888);
		canvas.drawBitmap(image, x + offsetX, y + offsetY, paint);
	}
	
	public Bitmap getImage() {
		return image;
	}
}
