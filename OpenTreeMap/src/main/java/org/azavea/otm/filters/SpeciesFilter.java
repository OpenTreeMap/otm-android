package org.azavea.otm.filters;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import org.azavea.helpers.Logger;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Species;
import org.azavea.otm.ui.SpeciesListDisplay;
import org.json.JSONException;
import org.json.JSONObject;

public class SpeciesFilter extends BaseFilter {
    static final public int SPECIES_SELECTOR = 1;

    public Species species = null;

    public SpeciesFilter(String key, String identifier, String label) {
        super(key, identifier, label);
    }

    @Override
    public boolean isActive() {
        return species != null;
    }

    @Override
    public View createView(LayoutInflater inflater, Activity activity) {
        View view = inflater.inflate(R.layout.filter_species_control, null);
        Button button = ((Button) view.findViewById(R.id.species_filter));
        updateSpecies(view, species);
        button.setOnClickListener(v -> {
            Intent speciesSelector = new Intent(App.getAppInstance(),
                    SpeciesListDisplay.class);
            activity.startActivityForResult(speciesSelector, SPECIES_SELECTOR);
        });
        return view;
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
    public void clear(View view) {
        this.species = null;
        updateSpecies(view, null);
    }

    @Override
    public JSONObject getFilterObject() {
        try {
            return buildNestedFilter(this.identifier, "IS", this.species.getId());
        } catch (JSONException e) {
            Logger.error("Error building species filter object", e);
            return null;
        }
    }

    public void updateSpecies(View view, Species species) {
        String name = "Not filtered";
        if (species != null) {
            name = species.getCommonName();
        }
        Button button = ((Button) view.findViewById(R.id.species_filter));
        view.setTag(R.id.species_id, species);
        button.setText(label + ": " + name);
    }
}
