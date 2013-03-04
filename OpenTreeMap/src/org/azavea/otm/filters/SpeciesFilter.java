package org.azavea.otm.filters;

import org.azavea.otm.R;
import org.azavea.otm.data.Species;
import org.json.JSONException;

import com.loopj.android.http.RequestParams;

import android.view.View;

public class SpeciesFilter extends BaseFilter {
	public Species species = null;
	
	public SpeciesFilter(String cqlKey, String nearestPlotKey, String name) {
		this.cqlKey = cqlKey;
		this.nearestPlotKey = nearestPlotKey;
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
	public void clear() {
		this.species = null;
	}

	@Override
	public void addToCqlRequestParams(RequestParams rp) {
		try {
			if (isActive()) {
				rp.put(cqlKey,  ""+ species.getId());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void addToNearestPlotRequestParams(RequestParams rp) {
		try {
			if (isActive()) {
				rp.put(nearestPlotKey,  ""+ species.getId());
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
}
