package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.RequestParams;

public class User extends Model {
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
}
