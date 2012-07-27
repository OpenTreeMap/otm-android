package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Geometry extends Model {
	public Geometry() {
		data = new JSONObject();
	}
	
	public Geometry(int srid, long lat, long lon) throws JSONException {
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
	
	public long getLat() throws JSONException {
		return data.getLong("lat");
	}
	
	public void setLat(long lat) throws JSONException {
		data.put("lat", lat);
	}
	
	public long getLon() throws JSONException {
		return data.getLong("lat");
	}
	
	public void setLon(long lon) throws JSONException {
		data.put("lng", lon);
	}
}
