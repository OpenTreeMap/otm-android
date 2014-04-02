package org.azavea.otm.filters;

import com.loopj.android.http.RequestParams;

/**
 * Boolean on/off filter for missing field
 */
public class MissingFilter extends BooleanFilter {

    public MissingFilter(String key, String identifier, String label) {
        super(key, identifier, label);
    }

    public MissingFilter(String key, String identifier, String label,
            boolean active) {
        super(key, identifier, label, active);
    }

    @Override
    public void addToCqlRequestParams(RequestParams rp) {
        // TODO Auto-generated method stub
    }

    @Override
    public void addToNearestPlotRequestParams(RequestParams rp) {
        // TODO: Not Implemented
    }

}
