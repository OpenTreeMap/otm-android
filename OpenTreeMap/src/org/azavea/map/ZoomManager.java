package org.azavea.map;

import android.util.Log;

// Helper class to handle zoom calculations
public class ZoomManager {
	private int width;
	private int height;
	private float zoomFactor;
	private int initialZoomLevel;
	private int zoomLevel;
	
	public ZoomManager() {
		width = 0;
		height = 0;
	}
	
	public ZoomManager(int width, int height) {
		this.width = width;
		this.height = height;
	}
	
	public ZoomManager(int width, int height, int initialZoomLevel) {
		this(width, height);
		this.initialZoomLevel = initialZoomLevel;
		this.setZoomLevel(initialZoomLevel);
	}
	
	public float getWidth() {
//		Log.d("getWidth", "Returning " + width*zoomFactor);
		return width * zoomFactor;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public float getHeight() {
//		Log.d("getHeight", "Returning " + height*zoomFactor);
		return height * zoomFactor;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public int getInitialZoomLevel() {
		return initialZoomLevel;
	}
	
	public void setInitialZoomLevel(int initialZoomLevel) {
		this.initialZoomLevel = initialZoomLevel;
	}
	
	// Having zoomed a tile but kept its center in the same place
	// how far away is the left/right of the tile from where it was?
	public float getWidthOffset() {
		return (width * zoomFactor - width) / 2;
	}

	// Having zoomed a tile but kept its center in the same place
	// how far away is the top/bottom of the tile from where it was?
	public float getHeightOffset() {
		return (height * zoomFactor - height) / 2;
	}
	
	public int getZoomLevel() {
		return zoomLevel;
	}
	
	public void setZoomLevel(int zoomLevel) {
		this.zoomLevel = zoomLevel;
		zoomFactor = (float)Math.pow(2, zoomLevel - initialZoomLevel);
//		Log.d("MapChangeViewLister", "zoomFactor before ternary equals " + zoomFactor);
//		zoomFactor = zoomFactor==0.0?(float)1.0:zoomFactor;
	}
	
	public float getZoomFactor() {
		return zoomFactor;
	}
}
