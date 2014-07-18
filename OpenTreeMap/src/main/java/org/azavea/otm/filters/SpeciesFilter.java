package org.azavea.otm.filters;

import org.azavea.otm.R;
import org.azavea.otm.data.Species;
import org.json.JSONException;
import org.json.JSONObject;

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
            this.species = (Species) species;
        } else {
            this.species = null;
        }
    }

    @Override
    public void clear() {
        this.species = null;
    }

    @Override
    public JSONObject getFilterObject() {
        try {
            return buildNestedFilter(this.identifier, "IS", this.species.getId());
        } catch (JSONException e) {
            return null;
        }
    }
}
