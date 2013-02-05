package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

public class UserType extends Model {
	public static final int ADMINISTRATOR_LEVEL = 1000;
	
	public UserType() {
		data = new JSONObject();
	}
	
	public int getLevel() throws JSONException {
		return data.getInt("level");
	}
	
	public void setLevel(int level) throws JSONException {
		data.put("level", level);
	}
	
	public String getName() throws JSONException {
		return data.getString("name");
	}
	
	public void setName(String name) throws JSONException {
		data.put("name", name);
	}
	
}
