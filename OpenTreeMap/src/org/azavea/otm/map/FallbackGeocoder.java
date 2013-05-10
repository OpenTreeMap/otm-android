package org.azavea.otm.map;

import java.io.IOException;
import java.util.List;

import org.azavea.otm.ui.MainMapActivity;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;

public class FallbackGeocoder {
	
	// search box
	private double lowerLeftLat;
	private double lowerLeftLon;
	private double upperRightLat;
	private double upperRightLon;
	
	private Context context;
	
	public FallbackGeocoder(Context context, double lowerLeftLat, double lowerLeftLon, double upperRightLat, double upperRightLon) {
		this.context = context;
		this.lowerLeftLat = lowerLeftLat;
		this.lowerLeftLon = lowerLeftLon;
		this.upperRightLat = upperRightLat;
		this.upperRightLon = upperRightLon;
	}

	public LatLng geocode(String address) {	
		LatLng pos = androidGeocode(address);
		//if (pos == null) {
		//	pos = httpGeocode(address);
		//}
		return pos;
	}
	
	public LatLng androidGeocode(String address) {
		Geocoder g = new Geocoder(this.context);
		try {
			List<Address> a = g.getFromLocationName(
					address, 
					1, 
					this.lowerLeftLat, 
					this.lowerLeftLon, 
					this.upperRightLat,
					this.upperRightLon
					);
			if (a.size() == 0) {
				return null;
			} else {
				Address geocoded = a.get(0);
				return new LatLng(geocoded.getLatitude(), geocoded.getLongitude());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*public LatLng httpGeocode(String address) {
		
	}
	*/
}
