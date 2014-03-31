package org.azavea.otm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class FieldManager {

    // All fields loaded from configuration file
    private ArrayList<FieldGroup> allDisplayFields = new ArrayList<FieldGroup>();

    // All field definitions from the api server, these may not be meant for
    // viewing for this application
    private Map<String, JSONObject> baseFields = new HashMap<String, JSONObject>();

    public FieldManager(JSONObject fieldDefinitions, JSONArray displayList)
            throws Exception {
        setBaseFieldDefinitions(fieldDefinitions);
        loadFieldDefinitions(displayList);
    }

    public Locale getLocale() {
        return new Locale(App.getAppInstance().getString(
                R.string.iso_locale_language), App.getAppInstance().getString(
                R.string.iso_locale_country));
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
}
