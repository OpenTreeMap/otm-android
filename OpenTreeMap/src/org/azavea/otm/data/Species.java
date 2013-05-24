package org.azavea.otm.data;

import org.json.JSONException;
import org.json.JSONObject;


public class Species extends Model implements Comparable<Object> {
	public Species() {
		data = new JSONObject();
	}
	
	public Species(int id, String scientificName, String commonName) throws JSONException {
		this();
		this.setCommonName(commonName);
		this.setScientificName(scientificName);
		this.setId(id);
	}
	
	public int getId() throws JSONException {
		return data.getInt("id");
	}

	public void setId(int id) throws JSONException {
		data.put("id", id);
	}

	public String getScientificName() throws JSONException {
		return data.getString("scientific_name");
	}

	public void setScientificName(String scientificName) throws JSONException {
		data.put("scientific_name", scientificName);
	}

	public String getCommonName() throws JSONException {
		return data.getString("common_name");
	}

	public void setCommonName(String commonName) throws JSONException {
		data.put("common_name", commonName);
	}

	public String getSpecies() throws JSONException {
		return data.getString("species");
	}

	public void setSpecies(String species) throws JSONException {
		data.put("species", species);
	}
	
	public String getGenus() throws JSONException {
		return data.getString("genus");
	}

	public void setGenus(String genus) throws JSONException {
		data.put("genus", genus);
	}
	

	public int compareTo(Object otherOne) 
	{
		Species other = (Species)otherOne;
		try {
			if (getCommonName().compareTo(other.getCommonName()) < 0) return -1;
			if (getCommonName().compareTo(other.getCommonName())  > 0) return 1;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	  return 0;
	}

	@Override
	public String toString() {
		try {
			// Whatever value toString provides to an ArrayAdapter will be used
			// in a simple wildcard filter.  By providing both names, both names
			// can be filtered on
			return getCommonName() + " " + getScientificName();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
