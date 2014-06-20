package org.azavea.otm;

import org.json.JSONException;
import org.json.JSONObject;

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
	
	public void set(Object newValue) throws JSONException {
		if (newValue == null || newValue.equals(null)) {
			json.put(key, JSONObject.NULL);
			
		} else if (newValue instanceof Integer) {
			json.put(key, Integer.parseInt(newValue.toString()));
			
		} else if (newValue instanceof Double) {
			json.put(key, Double.parseDouble(newValue.toString()));

		} else {
			json.put(this.key, newValue);
			
		}

	}

}
