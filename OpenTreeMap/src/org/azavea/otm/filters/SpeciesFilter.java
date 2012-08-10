package org.azavea.otm.filters;

import org.azavea.otm.R;
import org.azavea.otm.data.Species;
import org.json.JSONException;

import android.view.View;

public class SpeciesFilter extends BaseFilter {
	public Species species = null;
	
	public SpeciesFilter(String key, String name) {
		this.key = key;
		this.label = name;	
	}
	
	@Override
	public boolean isActive() {
		return species != null;
	}

	@Override
	public void updateFromView(View view) {
		Object species = view.getTag(R.id.species_id);
		if (species != null) {
			this.species = (Species)species;
		} else {
			this.species = null;
		}
	}

	@Override
	public String toQueryStringParam() {
		try {
			return isActive() ? key + "=" + species.getId() : "";
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}

	@Override
	public void clear() {
		this.species = null;
	}

}
