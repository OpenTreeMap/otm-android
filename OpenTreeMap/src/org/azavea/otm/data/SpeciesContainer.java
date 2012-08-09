package org.azavea.otm.data;

import org.json.JSONException;

public class SpeciesContainer extends ModelContainer<Species> {

	@Override
	public Species[] getAll() {
		Species[] species = new Species[data.length()];
		for (int i = 0; i < data.length(); i++) {
			species[i] = new Species();
			try {
				species[i].setData(data.getJSONObject(i));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return species;
	}

}
