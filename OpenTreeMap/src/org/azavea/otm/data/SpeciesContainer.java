package org.azavea.otm.data;

import java.util.LinkedHashMap;

import org.json.JSONException;

public class SpeciesContainer extends ModelContainer<Species> {

	@Override
	public LinkedHashMap<Integer,Species> getAll() {
		LinkedHashMap<Integer,Species> speciesList = 
				new LinkedHashMap<Integer,Species>(data.length());
		for (int i = 0; i < data.length(); i++) {
			try {
				Species species = new Species();
				species.setData(data.getJSONObject(i));
				speciesList.put(species.getId(), species);
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return speciesList;
	}

}
