package org.azavea.otm.filters;

import org.azavea.otm.R;
import org.azavea.otm.data.Species;

import com.loopj.android.http.RequestParams;

import android.view.View;

public class SpeciesFilter extends BaseFilter {
	public Species species = null;
	
	public SpeciesFilter(String key, String identifier, String label) {
		this.key = key;
		this.identifier = identifier;
		this.label = label;	
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
	public void clear() {
		this.species = null;
	}

	@Override
	public void addToCqlRequestParams(RequestParams rp) {
	    // TODO: Not Implemented
	}

	@Override
	public void addToNearestPlotRequestParams(RequestParams rp) {
	    // TODO: Not Implemented
	}
}
