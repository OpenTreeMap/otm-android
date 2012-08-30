package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;

public class Geometry extends Model {
	public Geometry() {
		data = new JSONObject();
	}
	
	public Geometry(int srid, double lat, double lon) throws JSONException {
		this();
		setSrid(srid);
		setLat(lat);
		setLon(lon);
	}
	
	public int getSrid() throws JSONException {
		return data.getInt("srid");
	}
	
	public void setSrid(int srid) throws JSONException {
		data.put("srid", srid);
	}
	
	public double getLat() throws JSONException {
		return data.getDouble("lat");
	}
	public double getLatE6() throws JSONException {
		return this.getLat()*1E6;
	}
	
	public void setLat(double lat) throws JSONException {
		data.put("lat", lat);
	}
	
	public double getLon() throws JSONException {
		return data.getDouble("lng");
	}
	public double getLonE6() throws JSONException {
		return this.getLon()*1E6;
	}
	public void setLon(double lon) throws JSONException {
		data.put("lng", lon);
	}
	
	public RequestParams toParams() throws JSONException {
		return new RequestParams();
	}
}
