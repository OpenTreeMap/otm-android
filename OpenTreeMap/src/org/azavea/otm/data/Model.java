package org.azavea.otm.data;

import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;

import android.util.Log;

// This is a bit of a hack but it enables us to have a generic
// handler as opposed to one for each data class
public abstract class Model {
	protected JSONObject data;
//	protected Hashtable<String, String> fieldMappings;
//	
//	public void populate(JSONObject data) {
//		try {
//			Class thisClass = this.getClass();
//			Enumeration<String> keys = fieldMappings.keys();
//			while(keys.hasMoreElements()) {				
//				String fieldName = keys.nextElement();
//				Field instanceVariable = thisClass.getDeclaredField(fieldMappings.get(fieldName));
//
//				Class instVarType = instanceVariable.getType();
//				Object value = data.get(fieldName);
//				
//				if (!value.equals(null)) {
//					if (instVarType.equals(String.class)) {
//						instanceVariable.set(this, value.toString());
//					} else if (instVarType.equals(int.class) || instVarType.equals(Integer.class)) {
//						instanceVariable.set(this, (Integer)value);
//					} else if (instVarType.equals(double.class) || instVarType.equals(Double.class)) {
//						instanceVariable.set(this, (Double)value);
//					} else if (instVarType.equals(boolean.class) || instVarType.equals(Boolean.class)) {
//						instanceVariable.set(this, (Boolean)value);
//					} else if (Model.class.isAssignableFrom(instVarType)) {
//						Model valObj = (Model)instVarType.newInstance();
//						valObj.populate(data.getJSONObject(fieldName));
//						instanceVariable.set(this, instVarType.cast(valObj));
//					}
//				}
//			}
//		} catch (Exception e) {
//			Log.e("Exception in Model.populate(): ", e.getMessage());
//		}
//	}
	
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
