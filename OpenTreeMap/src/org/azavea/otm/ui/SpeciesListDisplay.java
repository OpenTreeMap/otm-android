package org.azavea.otm.ui;


import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.FilterManager;
import org.azavea.otm.adapters.SpeciesAdapter;
import org.azavea.otm.data.Species;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SpeciesListDisplay extends FilterableListDisplay<Species> {

    @Override
    protected int getFilterHintTextId() {
        return R.string.filter_species_hint;
    }

    @Override
    public void onCreate(Bundle data) {
        super.onCreate(data);

        FilterManager search = App.getFilterManager();

        if (search.getSpecies().size() > 0) {
            renderSpeciesList();
        } else {
            search.loadSpeciesList(msg -> {
                if (msg.getData().getBoolean("success")) {
                    renderSpeciesList();
                } else {
                    Toast.makeText(App.getAppInstance(), "Could not get species list",
                            Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }
    }

    private void renderSpeciesList() {
        LinkedHashMap<Integer, Species> list = App.getFilterManager().getSpecies();

        Species[] species = list.values().toArray(new Species[list.size()]);

        // Sort by common name
        Arrays.sort(species);

        // Bind the custom adapter to the view
        SpeciesAdapter adapter = new SpeciesAdapter(this, R.layout.species_list_row, species);
        Log.d(App.LOG_TAG, list.size() + " species loaded");

        renderList(adapter);
    }
}
