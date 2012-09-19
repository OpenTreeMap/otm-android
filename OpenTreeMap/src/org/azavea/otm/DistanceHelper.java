package org.azavea.otm;

public class DistanceHelper {
	public static double gps2m(double lat1, double lng1, double lat2, double lng2) {
		 double latRad1 = Math.toRadians(lat1);
		    double latRad2 = Math.toRadians(lat2);
		    double lngRad1 = Math.toRadians(lng1);
		    double lngRad2 = Math.toRadians(lng2);

		    double dist = Math.acos(Math.sin(latRad1) * Math.sin(latRad2) + Math.cos(latRad1) * Math.cos(latRad2) * Math.cos(lngRad1 - lngRad2));
		    if(dist < 0) {
		        dist = dist + Math.PI;
		    }
		    return Math.round(dist * 6378100);//in meters
		}
}
