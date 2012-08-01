package org.azavea.otm.data;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;

import android.util.Log;

public abstract class Model {
	protected JSONObject data;
	
	protected long getLongOrDefault(String key, Long defaultValue) throws JSONException {
		if (data.isNull(key)){ 
			return defaultValue;
		} else {
			return data.getLong(key);
		}
	}
	public void setData(JSONObject data) {
		this.data = data;
	}
	
	public JSONObject getData() {
		return data;
	}
}
