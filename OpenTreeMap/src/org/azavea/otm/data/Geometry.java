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
		setY(lat);
		setX(lon);
	}
	
	public int getSrid() throws JSONException {
		return data.getInt("srid");
	}
	
	public void setSrid(int srid) throws JSONException {
		data.put("srid", srid);
	}
	
	public double getY() throws JSONException {
        return data.getDouble("y");
	}
	public double getYE6() throws JSONException {
		return this.getY()*1E6;
	}
	
	public void setY(double lat) throws JSONException {
		data.put("lat", lat);
	}
	
	public double getX() throws JSONException {
        return data.getDouble("x");
	}

	public double getXE6() throws JSONException {
		return this.getX()*1E6;
	}
	public void setX(double lon) throws JSONException {
		data.put("x", lon);
	}
	
	public RequestParams toParams() throws JSONException {
		return new RequestParams();
	}
}
