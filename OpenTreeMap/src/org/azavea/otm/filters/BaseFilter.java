package org.azavea.otm.filters;

import org.azavea.otm.App;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.View;

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

    /**
     *  Checks if this filter currently has an active value
     */
    public abstract boolean isActive();

    /**
     *  Update the value of the filter, and its active status
     *  from a view of the corresponding type
     */
    public abstract void updateFromView(View view);

    /**
     *  Reset the filter to the default state
     */
    public abstract void clear();

    /**
     *  Called when the filter is active...
     *  Gets the filter object representation of this filter
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
            Log.e(App.LOG_TAG, "Error creating JSONObject for filter", e);
        }

        return filter;
    }
}
