package org.azavea.otm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.azavea.otm.data.Species;
import org.azavea.otm.data.SpeciesContainer;
import org.azavea.otm.filters.BaseFilter;
import org.azavea.otm.filters.BooleanFilter;
import org.azavea.otm.filters.ChoiceFilter;
import org.azavea.otm.filters.MissingFilter;
import org.azavea.otm.filters.RangeFilter;
import org.azavea.otm.filters.SpeciesFilter;
import org.azavea.otm.rest.RequestGenerator;
import org.azavea.otm.rest.handlers.ContainerRestHandler;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.util.Log;
import android.view.View;

public class FilterManager {
    private RequestGenerator request = new RequestGenerator();

    // All filters loaded from configuration file, set with the latest state
    private LinkedHashMap<String, BaseFilter> allFilters = new LinkedHashMap<String, BaseFilter>();

    // List of all species received from the API
    private LinkedHashMap<Integer, Species> species = new LinkedHashMap<Integer, Species>();

    public FilterManager(JSONObject filterDefinitions) throws Exception {
        Log.d(App.LOG_TAG, "Creating new instance of Filter Manager");
        loadSpeciesList();
        loadFilterDefinitions(filterDefinitions);
    }

    public void loadSpeciesList() {
    }

    {
        loadSpeciesList(null);
    }

    public void loadSpeciesList(final Callback callback) {

        // If species were already lazy loaded, return immediately
        if (species.size() > 0 && callback != null) {
            handleSpeciesCallback(callback, true);
            return;
        }

        request.getAllSpecies(new ContainerRestHandler<SpeciesContainer>(
                new SpeciesContainer()) {

            @Override
            public void dataReceived(SpeciesContainer container) {
                try {
                    species = (LinkedHashMap<Integer, Species>) container
                            .getAll();
                    if (callback != null) {
                        handleSpeciesCallback(callback, true);
                    }
                } catch (JSONException e) {
                    Log.e(App.LOG_TAG, "Error in Species retrieval", e);
                }
            }

            @Override
            public void onFailure(Throwable e, String message) {
                Log.e(App.LOG_TAG, message, e);
                if (callback != null) {
                    handleSpeciesCallback(callback, false);
                }
            }
        });
    }

    private void handleSpeciesCallback(Callback callback, boolean success) {
        Message resultMessage = new Message();
        Bundle data = new Bundle();
        data.putBoolean("success", success);
        resultMessage.setData(data);
        callback.handleMessage(resultMessage);
    }

    private BaseFilter makeMapFilter(String key, String identifier,
            String label, String type, JSONArray choices) throws Exception {

        if (type.equals("BOOL")) {
            return new BooleanFilter(key, identifier, label);
        } else if (type.equals("RANGE")) {
            return new RangeFilter(key, identifier, label);
        } else if (type.equals("SPECIES")) {
            return new SpeciesFilter(key, identifier, label);
        } else if (type.equals("MISSING")) {
            return new MissingFilter(key, identifier, label);
        } else if (type.equals("CHOICE")) {
            return new ChoiceFilter(key, identifier, label, choices);
        } else {
            throw new Exception("Invalid filter type defined in config: "
                    + type);
        }
    }

    private void loadFilterDefinitions(JSONObject filterGroups) {

        loadFilterDef(filterGroups.optJSONArray("standard"), false);
        loadFilterDef(filterGroups.optJSONArray("missing"), true);
    }

    private void loadFilterDef(JSONArray filterDefs, Boolean isMissing) {
        if (filterDefs == null || filterDefs.length() == 0) {
            return;
        }

        String keyPrefix = isMissing ? "m:" : "s:";

        for (int i = 0; i < filterDefs.length(); i++) {
            try {
                JSONObject def = filterDefs.getJSONObject(i);
                String identifier = def.getString("identifier");
                String key = keyPrefix + identifier;
                String label = def.getString("label");
                String type = def.optString("search_type");
                if (isMissing) {
                    type = "MISSING";
                }
                JSONArray choices = def.optJSONArray("choices");

                BaseFilter filter = makeMapFilter(key, identifier, label, type,
                        choices);
                allFilters.put(key, filter);

            } catch (Exception e) {
                Log.e(App.LOG_TAG, "Could not create a filter from def # " + i,
                        e);
            }
        }
    }

    /**
     * All species objects indexed by their id.
     */
    public LinkedHashMap<Integer, Species> getSpecies() {
        return species;
    }

    public Species getSpecieById(int id) {
        return species.get(id);
    }

    public BaseFilter getFilter(String key) {
        return allFilters.get(key);
    }

    public LinkedHashMap<String, BaseFilter> getFilters() {
        return allFilters;
    }

    /**
     * Reset all active filters to their default state
     */
    public void clearActiveFilters() {
        for (LinkedHashMap.Entry<String, BaseFilter> entry : allFilters
                .entrySet()) {
            entry.getValue().clear();
        }
    }

    /**
     * Update the values of a given filter from a filter view control
     * 
     * @param key
     *            - The filter key
     * @param view
     *            - The view which contains value for the filter
     */
    public void updateFilterFromView(String key, View view) {
        allFilters.get(key).updateFromView(view);
    }

    /**
     * Returns a comma separated string of active filter names
     * 
     */
    public String getActiveFilterDisplay() {
        String display = "", sep = "";
        for (Map.Entry<String, BaseFilter> entry : allFilters.entrySet()) {
            BaseFilter filter = entry.getValue();
            if (filter.isActive()) {
                display += sep + filter.label;
                sep = ", ";
            }
        }
        return display;
    }

    /**
     * Returns a RequestParams object loaded with the filter values.
     */
    public Collection<JSONObject> getActiveFilters() {
        List<JSONObject> filterObjects = new ArrayList<JSONObject>();
        for (BaseFilter filter : allFilters.values()) {
            if (filter.isActive()) {
                filterObjects.add(filter.getFilterObject());
            }
        }
        return filterObjects;
    }
}
