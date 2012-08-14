package org.azavea.map;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

public class TileProvider {
	private GeoPoint topLeft;
	private GeoPoint bottomRight;
	private double tileWidth; // geographic width of a tile
	private double tileHeight; // geographic height of a tile
	private int numTilesX;
	
	public TileProvider() {
		topLeft = new GeoPoint(0, 0);
		bottomRight = new GeoPoint(0, 0);
	}
	
	public TileProvider(GeoPoint topLeft, GeoPoint bottomRight, int numTilesX, int numTilesY) {
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;

		int diffX = bottomRight.getLongitudeE6() - topLeft.getLongitudeE6();
		int diffY = topLeft.getLatitudeE6() - bottomRight.getLatitudeE6();

		tileWidth = (diffX) / (numTilesX - 2); // We want number of /display/-tiles
		tileHeight = (diffY) / (numTilesY - 2);
		
		this.numTilesX = numTilesX;
	}
	
	public Tile getTile(int x, int y) {
		int actualX = x-1;
		int actualY = y-1;
		double tileLeft = topLeft.getLongitudeE6() + actualX * tileWidth;
		double tileTop = topLeft.getLatitudeE6() - actualY * tileHeight;
		double tileRight = bottomRight.getLongitudeE6() + actualX * tileWidth;
		double tileBottom = bottomRight.getLatitudeE6() - actualY * tileHeight;
		
		double left = tileLeft/1E6;
		double top = tileTop/1E6;
		double right = tileRight/1E6;
		double bottom = tileBottom/1E6;
		
		Log.d("", "actualX = " + actualX + ", actualY = " + actualY + ", left = " + left + ", top = " + top + ", right = " + right + ", bottom = " + bottom);
		
		Bitmap tileImage = WMSClient.getTile(left, top, right, bottom, Tile.HEIGHT, Tile.WIDTH);
		

		return new Tile(actualX * Tile.WIDTH, actualY * Tile.HEIGHT, (int)tileTop, (int)tileLeft, tileImage);
	}
	
	// Note that if the tiles in the grid have been shifted right and down,
	// the equivalent for the viewport is to move left and up
	public void moveViewport(int shiftRight, int shiftUp) {
		Log.d("", "tileWidth = " + tileWidth + ", tileHeight = " + tileHeight);
		int tlLat = topLeft.getLatitudeE6();
		int tlLon = topLeft.getLongitudeE6();
		int brLat = bottomRight.getLatitudeE6();
		int brLon = bottomRight.getLongitudeE6();
		
		tlLat += shiftUp * tileHeight;

		tlLon += shiftRight * tileWidth;
		
		brLat += shiftUp * tileHeight;
		
		brLon += shiftRight * tileWidth;
		
		topLeft = new GeoPoint(tlLat, tlLon);
		bottomRight = new GeoPoint(brLat, brLon);
		Log.d("", "New viewport: " + tlLat + "," + tlLon + " -> " + brLat + "," + brLon);
	}
}
