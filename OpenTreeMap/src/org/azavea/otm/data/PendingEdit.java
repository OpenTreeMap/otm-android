package org.azavea.otm.data;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

public class PendingEdit {
	private JSONObject data;
	
	public PendingEdit(JSONObject definition) {
		data = definition;
	}
	
	public String getUsername() throws JSONException {
		return data.getString("username");
	}
	
	public String getValue() throws JSONException {
		return data.getString("value");
	}
	
	public int getId() throws JSONException {
		return data.getInt("id");
	}
	
	public Date getSubmittedTime() throws Exception {
		String when = data.getString("submitted");
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss'");
		return format.parse(when);
	}
	
	public void approve() throws Exception {
		// TODO
		throw new Exception("not implemented");
	}
	
	public void setData(JSONObject data) {
		this.data = data;
	}
}
