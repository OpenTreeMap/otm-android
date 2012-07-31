package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

public class User extends Model{
	public String password;

	public User() {
		data = new JSONObject();
	}
	
	public int getId() throws JSONException {
		return data.getInt("id");
	}
	
	public void setId(int id) throws JSONException {
		data.put("id", id);
	}
	
	public String getFirstName() throws JSONException {
		return data.getString("firstname");
	}
	
	public void setFirstName(String firstName) throws JSONException {
		data.put("firstname", firstName);
	}
	
	public String getLastName() throws JSONException {
		return data.getString("lastname");
	}
	
	public void setLastName(String lastName) throws JSONException {
		data.put("lastname", lastName);
	}
	
	public String getEmail() throws JSONException {
		return data.getString("email");
	}
	
	public void setEmail(String email) throws JSONException {
		data.put("email", email);
	}
	
	public String getUsername() throws JSONException {
		return data.getString("username");
	}
	
	public void setusername(String username) throws JSONException {
		data.put("username", username);
	}
	
	public int getZipCode() throws JSONException {
		return data.getInt("zipcode");
	}
	
	public void setZipCode(int zipCode) throws JSONException {
		data.put("zipcode", zipCode);
	}

	public int getReputation() throws JSONException {
		return data.getInt("reputation");
	}
	
	public void setReputation(int reputation) throws JSONException {
	
		data.put("reputation", reputation);
	}
	
	public UserType getUserType() throws JSONException {
		UserType type = new UserType();
		type.setData(data.getJSONObject("user_type"));
		return type;
	}
	
	public void setUserType(UserType type) throws JSONException {
		data.put("user_type", type.getData());
	}
}
