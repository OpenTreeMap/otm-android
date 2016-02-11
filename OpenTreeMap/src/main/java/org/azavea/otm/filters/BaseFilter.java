package org.azavea.otm.filters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import org.azavea.helpers.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public abstract class BaseFilter {
    /**
     * A unique, deterministic key for the filter - since the same identifier
     * can be used in multiple filters (missing, standard).
     */
    public String key;

    /**
     * Filter field identifier
     */
    public String identifier;

    /**
     * The name to display as a filter label
     */
    public String label;

    protected BaseFilter(String key, String identifier, String label) {
        this.key = key;
        this.identifier = identifier;
        this.label = label;
    }

    /**
     * Checks if this filter currently has an active value
     */
    public abstract boolean isActive();

    /**
     * Creates and returns the View that represents this filter
     */
    public abstract View createView(LayoutInflater inflater, Activity activity);

    /**
     * Update the value of the filter, and its active status
     * from a view of the corresponding type
     */
    public abstract void updateFromView(View view);

    /**
     * Reset the filter to the default state
     */
    public abstract void clear(View view);

    /**
     * Called when the filter is active...
     * Gets the filter object representation of this filter
     */
    public abstract JSONObject getFilterObject();

    protected JSONObject buildNestedFilter(String identifier, String predicate, Object value) {
        JSONObject filter = null;
        JSONObject predicatePair = new JSONObject();

        try {
            filter = new JSONObject();
            predicatePair.put(predicate, value);
            filter.put(identifier, predicatePair);
        } catch (JSONException e) {
            Logger.error("Error building search filter", e);
        }

        return filter;
    }
}
