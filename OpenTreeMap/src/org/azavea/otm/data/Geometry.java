package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;

public class Geometry extends Model {
	public Geometry() {
		data = new JSONObject();
	}
	
	public Geometry(int srid, double x, double y) throws JSONException {
		this();
		setSrid(srid);
		setY(y);
		setX(x);
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
	
	public void setY(double y) throws JSONException {
		data.put("y", y);
	}
	
	public double getX() throws JSONException {
        return data.getDouble("x");
	}

	public void setX(double x) throws JSONException {
		data.put("x", x);
	}
	
	public RequestParams toParams() {
		return new RequestParams();
	}
	
	@Override
	public String toString() {
	    return String.format("(%s, %s)", 
	            this.data.optString("x"), this.data.optString("y"));
	}
}
