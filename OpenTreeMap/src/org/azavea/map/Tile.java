package org.azavea.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Tile {
//	public static final int HEIGHT = 800;
//	public static final int WIDTH = 480;
	private int zoomOffsetX;
	private int zoomOffsetY;
	
	private Bitmap image;
	
	public Tile() {
		zoomOffsetX = 0;
		zoomOffsetY = 0;
	}
	
	public Tile(Bitmap image) {
		this();
		this.image = image;
	}
	
	public void draw(Canvas canvas, int x, int y, int offsetX, int offsetY) {
		Paint paint = new Paint();
		paint.setAlpha(0x888);
		canvas.drawBitmap(image, x + offsetX + zoomOffsetX, y + offsetY + zoomOffsetY, paint);
	}
	
	public Bitmap getImage() {
		return image;
	}
	
	public void scale(float factor) {
		Matrix m = new Matrix();
		int oldWidth = image.getWidth();
		int oldHeight = image.getHeight();
		m.setScale(factor, factor);
		
		Bitmap scaledImage = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), m, true);
		
		image = scaledImage;
		
		computeZoomOffset(oldWidth, oldHeight, image.getWidth(), image.getHeight());
	}
	
	private void computeZoomOffset(int oldWidth, int oldHeight, int newWidth, int newHeight) {
		zoomOffsetX = newWidth - oldWidth;
		zoomOffsetY = newHeight - oldHeight;
	}
}
