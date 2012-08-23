package org.azavea.map;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.Log;

import com.google.android.maps.GeoPoint;

public class Tile {
	private Bitmap image;
	private int zoomOffsetX;
	private int zoomOffsetY;
	
	public Tile() {
	}
	
	public Tile(Bitmap image) {
		this();
		this.image = image;
	}
	
	public void draw(Canvas canvas, int x, int y, int tileWidth, int tileHeight, int offsetX, int offsetY, float zoomFactor) {
		Paint paint = new Paint();
		paint.setAlpha(0x888);
		float zoomOffsetX = 0;
		float zoomOffsetY = 0;
		
		zoomOffsetX = (tileWidth*zoomFactor - tileWidth) / 2;
		zoomOffsetY = (tileHeight*zoomFactor - tileHeight) / 2;
		
		if (zoomFactor == 1.0) {
			canvas.drawBitmap(image, x + offsetX, y + offsetY, paint);
		} else { // mapview is 365 high in emulator! pass height and width in, divide by zoom-factor and subtract from coords
			canvas.drawBitmap(image, (x-zoomOffsetX)/zoomFactor + offsetX/zoomFactor, (y-zoomOffsetY)/zoomFactor + offsetY/zoomFactor, paint);
		}
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
