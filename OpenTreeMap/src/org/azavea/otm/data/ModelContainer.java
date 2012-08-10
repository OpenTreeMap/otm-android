package org.azavea.otm.data;

import java.util.LinkedHashMap;

import org.json.JSONArray;

public abstract class ModelContainer<T> {
	protected JSONArray data;
	
	public void setData(JSONArray array) {
		this.data = array;
	}
	
	public JSONArray getData() {
		return data;
	}
	
	public abstract LinkedHashMap<Integer,T> getAll(); 
}
