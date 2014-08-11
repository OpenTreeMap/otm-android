package org.azavea.otm.filters;

import org.json.JSONObject;

/**
 * Boolean on/off filter for missing field
 */
public class MissingFilter extends BooleanFilter {

    public MissingFilter(String key, String identifier, String label) {
        super(key, identifier, label);
    }

    @Override
    public JSONObject getFilterObject() {
        return buildNestedFilter(this.identifier, "ISNULL", this.active);
    }

}
