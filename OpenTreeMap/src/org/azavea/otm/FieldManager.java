package org.azavea.otm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class FieldManager {

    // All fields loaded from configuration file
    private ArrayList<FieldGroup> allDisplayFields = new ArrayList<>();

    // All field definitions from the api server, these may not be meant for
    // viewing for this application
    private Map<String, JSONObject> baseFields = new HashMap<>();

    // Ordered list of display keys for plot eco benefits
    private String[] ecoKeys;

    public FieldManager(InstanceInfo instance)
            throws Exception {

        setBaseFieldDefinitions(instance.getFieldDefinitions());
        loadFieldDefinitions(instance.getDisplayFieldKeys());
        loadEcoBenefitKeys(instance.getPlotEcoFields());
    }

    private void loadEcoBenefitKeys(JSONObject plotEcoFields) {
        // Eco benefits aren't normal field definitions, but rather just an ordered
        // set of keys.  The field definitions are loaded with each plot.  If an
        // instance has no plot eco bens or keys defined, don't display any
        if (plotEcoFields == null) {
            ecoKeys = null;
            return;
        }
        JSONArray keys = plotEcoFields.optJSONArray("keys");
        if (keys == null) {
            ecoKeys = null;
            return;
        }
        ecoKeys = new String[keys.length()];
        for (int i = 0; i < keys.length(); i++) {
            ecoKeys[i] = keys.optString(i);
        }
    }

    private void setBaseFieldDefinitions(JSONObject fieldDefinitions)
            throws Exception {
        try {
            baseFields.clear();
            Iterator<?> keys = fieldDefinitions.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                baseFields.put(key, fieldDefinitions.getJSONObject(key));
            }

        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Bad Field Definition", e);
            throw new Exception("Incorrectly configured base field list");
        }
    }

    private void loadFieldDefinitions(JSONArray displayData) throws Exception {
        if (this.baseFields.isEmpty()) {
            throw new Exception(
                    "Cannot load field definitions, base fields have not been set");
        }

        try {
            for (int i = 0; i < displayData.length(); i++) {
                JSONObject fieldGroup = displayData.getJSONObject(i);
                allDisplayFields.add(new FieldGroup(fieldGroup, baseFields));
            }
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Unable to load field group", e);
            throw new Exception("Bad field group definition");
        }
    }

    public Field getField(String name) {
        for (FieldGroup group : allDisplayFields) {
            for (Map.Entry<String, Field> field : group.getFields().entrySet()) {
                if (field.getKey().equals(name)) {
                    return field.getValue();
                }
            }
        }
        return null;
    }

    public FieldGroup[] getFieldGroups() {
        return allDisplayFields
                .toArray(new FieldGroup[allDisplayFields.size()]);
    }

    public String[] getEcoKeys() {
        return ecoKeys;
    }
}
