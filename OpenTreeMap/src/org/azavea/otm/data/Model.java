package org.azavea.otm.data;

import android.util.Log;

import org.azavea.otm.App;
import org.azavea.otm.NestedJsonAndKey;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class Model {
    protected JSONObject data;

    protected String safeGetString(String key) {
        try {
            return data.isNull(key) ? null : data.getString(key);
        } catch (JSONException e) {
            return null;
        }
    }

    protected long getLongOrDefault(String key, Long defaultValue) throws JSONException {
        if (data.isNull(key)) {
            return defaultValue;
        } else {
            return data.getLong(key);
        }
    }

    protected double getDoubleOrDefault(String key, Double defaultValue) throws JSONException {
        if (data.isNull(key)) {
            return defaultValue;
        } else {
            return data.getDouble(key);
        }
    }

    public void setData(JSONObject data) {
        this.data = data;
    }

    public JSONObject getData() {
        return data;
    }

    public Object getField(String key) {
        return data.isNull(key) ? null : data.opt(key);
    }

    /**
     * Return the value of a key name, which can be nested using . notation. If
     * the key does not exist or the value of the key, it will return a null
     * value
     */
    public Object getValueForKey(String key) {
        try {
            String[] keys = key.split("\\.");
            NestedJsonAndKey found = getValueForKey(keys, 0, data, false);
            if (found != null) {
                return found.get();
            } else {
                return null;
            }
        } catch (Exception e) {
            Log.w(App.LOG_TAG, "Could not find key: " + key + " on plot/tree object");
            return null;
        }
    }

    public void setValueForKey(String key, Object value) throws Exception {
        try {
            String[] keys = key.split("\\.");
            NestedJsonAndKey found = getValueForKey(keys, 0, data, true);
            if (found != null) {
                found.set(value);
            } else {
                Log.w(App.LOG_TAG, "Specified key does not exist, cannot set value: " + key);
            }

        } catch (Exception e) {
            Log.w(App.LOG_TAG, "Could not set value key: " + key + " on plot/tree object");
            throw e;
        }
    }

    /**
     * Return value for keys, which could be nested as an array
     */
    private NestedJsonAndKey getValueForKey(String[] keys, int index, JSONObject json, boolean createNodeIfEmpty)
            throws JSONException {
        if (index < keys.length - 1 && keys.length > 1) {
            JSONObject child;
            if (json.isNull(keys[index]) && createNodeIfEmpty) {
                child = new JSONObject();
                json.put(keys[index], child);
            } else {
                child = json.getJSONObject(keys[index]);
            }

            index++;
            return getValueForKey(keys, index, child, createNodeIfEmpty);
        }

        // We care to distinguish between a null value and a missing key.
        if (json.has(keys[index])) {
            return new NestedJsonAndKey(json, keys[index]);
        } else if (createNodeIfEmpty) {
            // Create an empty node for this key
            return new NestedJsonAndKey(json.put(keys[index], ""), keys[index]);
        } else {
            return null;
        }

    }
}
