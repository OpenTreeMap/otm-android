package org.azavea.otm.filters;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class RangeFilter extends BaseFilter {
    private Double min;
    private Double max;

    public RangeFilter(String key, String identifier, String label) {
        this.key = key;
        this.identifier = identifier;
        this.label = label;
    }

    public String getMinString() {
        return this.min == null ? "" : Double.toString(this.min);
    }

    public String getMaxString() {
        return this.max == null ? "" : Double.toString(this.max);
    }

    @Override
    public boolean isActive() {
        return (min != null || max != null);
    }

    private Double parseNumber(View view, int rId) {
        String min = ((EditText) view.findViewById(rId)).getText()
                .toString().trim();
        if (min != null && !"".equals(min)) {
            return Double.parseDouble(min);
        }
        return null;
    }

    @Override
    public void updateFromView(View view) {
        this.min = parseNumber(view, R.id.min);
        this.max = parseNumber(view, R.id.max);
    }

    @Override
    public void clear() {
        this.min = null;
        this.max = null;
    }

    @Override
    public JSONObject getFilterObject() {
        JSONObject filter = null;
        JSONObject predicateFilter = new JSONObject();

        try {
            if (min != null) {
                predicateFilter.put("MIN", this.min);
            }
            if (max != null) {
                predicateFilter.put("MAX", this.max);
            }
            if (predicateFilter.length() > 0) {
                filter = new JSONObject();
                filter.put(this.identifier, predicateFilter);
            }
        } catch (JSONException e) {
            Log.e(App.LOG_TAG, "Error creating JSONObject for filter", e);
        }

        return filter;
    }
}
