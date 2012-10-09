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
		Log.d("json", "old: " + json.get(key));
		json.put(this.key, value);
		Log.d("json", "new: " + json.get(key));
	}
}
