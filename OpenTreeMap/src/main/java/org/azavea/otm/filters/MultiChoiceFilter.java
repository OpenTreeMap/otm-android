package org.azavea.otm.filters;

import org.json.JSONArray;
import org.json.JSONObject;

public class MultiChoiceFilter extends ChoiceFilter {
    public MultiChoiceFilter(String key, String identifier, String label, JSONArray choices) {
        super(key, identifier, label, choices);
    }

    @Override
    public JSONObject getFilterObject() {
        return buildNestedFilter(this.identifier, "LIKE", "\"" + choices[this.selectedIndex].getValue() + "\"");
    }
}
