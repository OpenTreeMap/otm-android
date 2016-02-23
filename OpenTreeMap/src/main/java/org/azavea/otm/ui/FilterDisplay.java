package org.azavea.otm.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.azavea.helpers.Logger;
import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Species;
import org.azavea.otm.fields.Field;
import org.azavea.otm.filters.BaseFilter;
import org.azavea.otm.filters.SpeciesFilter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class FilterDisplay extends UpEnabledActionBarActivity {
    private Map<BaseFilter, View> filterViews = newHashMap();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_activity);

        LinearLayout filter_list = (LinearLayout) findViewById(R.id.filter_list);
        createFilterUI(App.getFilterManager().getFilters(), filter_list);
    }

    public void onComplete(View view) {
        // Update any active filters from the view
        for (Map.Entry<BaseFilter, View> filterView : filterViews.entrySet()) {
            filterView.getKey().updateFromView(filterView.getValue());
        }
        setResult(RESULT_OK);
        finish();
    }

    public void onClear(View clearButton) {
        resetFilters();
    }

    /**
     * Notify all filter that they should clear their state to off
     */
    private void resetFilters() {
        for (Map.Entry<BaseFilter, View> filter : filterViews.entrySet()) {
            filter.getKey().clear(filter.getValue());
        }
    }

    private void createFilterUI(LinkedHashMap<String, BaseFilter> filters,
                                LinearLayout parent) {

        LayoutInflater layout = this.getLayoutInflater();
        for (Map.Entry<String, BaseFilter> entry : filters.entrySet()) {
            BaseFilter filter = entry.getValue();
            View view = filter.createView(layout, this);
            parent.addView(view);
            filterViews.put(filter, view);
        }
    }

    private void updateSpecies(Species species) {
        SpeciesFilter filter = getSpeciesFilter();
        if (filter != null) {
            filter.updateSpecies(filterViews.get(filter), species);
        }
    }

    private @Nullable SpeciesFilter getSpeciesFilter() {
        for (BaseFilter filter : filterViews.keySet()) {
            if (filter instanceof SpeciesFilter) {
                return (SpeciesFilter) filter;
            }
        }
        return null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case (SpeciesFilter.SPECIES_SELECTOR): {
                if (resultCode == Activity.RESULT_OK) {
                    CharSequence speciesJSON = data.getCharSequenceExtra(Field.TREE_SPECIES);
                    if (!JSONObject.NULL.equals(speciesJSON)) {
                        Species species = new Species();
                        try {

                            species.setData(new JSONObject(speciesJSON.toString()));
                            updateSpecies(species);

                        } catch (JSONException e) {
                            String msg = "Unable to retrieve selected species";
                            Logger.error(msg, e);
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                        }
                    }
                }
                break;
            }
        }
    }
}
