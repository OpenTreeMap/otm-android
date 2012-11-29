package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

public class Password extends Model {
	public Password() {
		data = new JSONObject();
	}
	
	public Password(String password) throws JSONException {
		this();
		setPassword(password);
	}
	
	public void setPassword(String password) throws JSONException {
		data.put("password", password);
	}
	
	public String getPassword() throws JSONException {
		return data.getString("password");
	}
}
