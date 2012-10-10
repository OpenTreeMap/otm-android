package org.azavea.otm;

import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class NestedJsonAndKey {

	public NestedJsonAndKey(JSONObject json, String key) {
		this.json = json;
		this.key = key;
	}
	
	public JSONObject json = null;
	public String key = null;
	
	public Object get() throws JSONException {
		return json.get(key);
		
	}
	
	public void set(Object value) throws JSONException {
		Object val = json.get(key);
		Log.d("json", key + " old: " + val);
		if ("".equals(val) || val.equals(null)) {
			json.put(key, null);
		} else if (value instanceof Integer) {
			Log.d("json", "I'm an int!");
			json.put(key, Integer.parseInt(value.toString()));
		} else if (value instanceof Double) {
			Log.d("json", "I'm an W!");
			json.put(key, Double.parseDouble(value.toString()));			
		} else {
			Log.d("json", "I'm probably a string?");
			json.put(this.key, value);
		}
			
		Log.d("json", "new: " + val);
	}

}
