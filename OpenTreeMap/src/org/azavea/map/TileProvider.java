package org.azavea.map;
/*
import org.azavea.otm.App;
import org.azavea.otm.rest.handlers.TileHandler;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;
import com.loopj.android.http.AsyncHttpResponseHandler;

public class TileProvider {
	private GeoPoint topLeft;
	private GeoPoint bottomRight;
	private int tileWidth;
	private int tileHeight;
	private double tileGeoWidth; // geographic width of a tile
	private double tileGeoHeight; // geographic height of a tile
	private float zoomFactor;
	private float zoomedGeoWidthOffset;
	private float zoomedGeoHeightOffset;
	
	public TileProvider() {
		topLeft = new GeoPoint(0, 0);
		bottomRight = new GeoPoint(0, 0);
		zoomFactor = 1.0f;
	}
	
	public TileProvider(GeoPoint topLeft, GeoPoint bottomRight, int numTilesX, int numTilesY, int tileWidth, int tileHeight) {
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;
		
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;

		int diffX = bottomRight.getLongitudeE6() - topLeft.getLongitudeE6();
		int diffY = topLeft.getLatitudeE6() - bottomRight.getLatitudeE6();

		tileGeoWidth = (diffX) / (numTilesX - 2); // We want number of /display/-tiles
		tileGeoHeight = (diffY) / (numTilesY - 2);
	}
	
	public void getTile(int x, int y, TileHandler response) {
		int actualX = x;
		int actualY = y;
		double tileLeft = topLeft.getLongitudeE6() + actualX * tileGeoWidth;
		double tileTop = topLeft.getLatitudeE6() - actualY * tileGeoHeight;
		double tileRight = bottomRight.getLongitudeE6() + actualX * tileGeoWidth;
		double tileBottom = bottomRight.getLatitudeE6() - actualY * tileGeoHeight;
		
		double left = tileLeft/1E6;
		double top = tileTop/1E6;
		double right = tileRight/1E6;
		double bottom = tileBottom/1E6;
		
		Log.d(App.LOG_TAG, "actualX = " + actualX + ", actualY = " + actualY + ", left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
		
		GeoRect boundingBox = new GeoRect(top, left, bottom, right);
		response.setBoundingBox(boundingBox);
		WMSClient.getTile(boundingBox, tileHeight, tileWidth, response);
	}
	
	// Note that if the tiles in the grid have been shifted right and down,
	// the equivalent for the viewport is to move left and up
	public void moveViewport(int shiftRight, int shiftDown) {
		int tlLat = topLeft.getLatitudeE6();
		int tlLon = topLeft.getLongitudeE6();
		int brLat = bottomRight.getLatitudeE6();
		int brLon = bottomRight.getLongitudeE6();
		
		tlLat -= shiftDown * tileGeoHeight + zoomedGeoHeightOffset;

		tlLon += shiftRight * tileGeoWidth - zoomedGeoWidthOffset;
		
		brLat -= shiftDown * tileGeoHeight - zoomedGeoHeightOffset;
		
		brLon += shiftRight * tileGeoWidth + zoomedGeoWidthOffset;
		
		topLeft = new GeoPoint(tlLat, tlLon);
		bottomRight = new GeoPoint(brLat, brLon);
		Log.d(App.LOG_TAG, "New viewport: " + tlLat + "," + tlLon + " -> " + brLat + "," + brLon);
	}
	
	public void setZoomFactor(float zoomFactor) {
		this.zoomFactor = zoomFactor;
		zoomedGeoHeightOffset = (float) ((tileGeoHeight * zoomFactor - tileGeoHeight) / 2);
		zoomedGeoWidthOffset = (float) ((tileGeoWidth * zoomFactor - tileGeoWidth) / 2);
	}
}
*/