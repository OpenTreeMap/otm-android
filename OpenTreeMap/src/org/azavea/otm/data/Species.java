package org.azavea.otm.data;

import org.azavea.otm.App;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Species extends Model {
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
	
	@Override
	public String toString() {
		try {
			return getCommonName();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		
	}
}
