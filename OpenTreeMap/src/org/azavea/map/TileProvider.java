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
	
	public TileProvider(GeoPoint topLeft, GeoPoint bottomRight, int numTilesX, int numTilesY, Projection proj) {
		this.topLeft = topLeft;
		this.bottomRight = bottomRight;

		int diffX = bottomRight.getLatitudeE6() - topLeft.getLatitudeE6();
		int diffY = topLeft.getLongitudeE6() - bottomRight.getLongitudeE6();

		tileWidth = (diffX) / (numTilesX - 2); // We want number of /display/-tiles
		tileHeight = (diffY) / (numTilesY - 2);
		
		this.numTilesX = numTilesX;
	}
	
	public Tile getTile(int x, int y) {
		int actualX = x-1;
		int actualY = y-1;
		double tileLeft = topLeft.getLatitudeE6() + actualX * tileWidth;
		double tileTop = topLeft.getLongitudeE6() - actualY * tileHeight;
		double tileRight = bottomRight.getLatitudeE6() + actualX * tileWidth;
		double tileBottom = bottomRight.getLongitudeE6() - actualY * tileHeight;
		
		double left = tileLeft/1E6;
		double top = tileTop/1E6;
		double right = tileRight/1E6;
		double bottom = tileBottom/1E6;
		
		Bitmap tileImage = WMSClient.getTile(top, left, bottom, right, Tile.HEIGHT, Tile.WIDTH);
		

		return new Tile(actualY * Tile.WIDTH, actualX * -1 * Tile.HEIGHT, (int)tileTop, (int)tileLeft, tileImage);
	}
}
