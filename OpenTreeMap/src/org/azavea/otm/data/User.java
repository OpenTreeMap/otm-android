package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

public class User extends Model {
	public static int ADMINISTRATOR_LEVEL = 0; //TODO 
	
	public User() throws JSONException {
		data = new JSONObject();
	}
	
	public User(String userName,
				String firstName,
				String lastName,
				String email,
				String password,
				String zipcode) throws JSONException {
		this();
		
		setUserName(userName);
		setFirstName(firstName);
		setLastName(lastName);
		setEmail(email);
		setPassword(password);
		setZipcode(zipcode);
	}
	
	public int getId() throws JSONException {
		return data.getInt("id");
	}
	
	public String getUserName() throws JSONException {
		return data.getString("username");
	}
	
	public void setUserName(String userName) throws JSONException {
		data.put("username", userName);
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
	
	public String getPassword() throws JSONException {
		return data.getString("password");
	}
	
	public void setPassword(String password) throws JSONException {
		data.put("password", password);
	}
	
	public String getZipcode() throws JSONException {
		return data.getString("zipcode");
	}
	
	public void setZipcode(String zipcode) throws JSONException {
		data.put("zipcode", zipcode);
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
