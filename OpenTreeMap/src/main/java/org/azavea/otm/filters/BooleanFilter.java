package org.azavea.otm.filters;

import org.azavea.otm.R;
import org.json.JSONObject;

import android.view.View;
import android.widget.ToggleButton;

public class BooleanFilter extends BaseFilter {
    public boolean active;

    public BooleanFilter(String key, String identifier, String label) {
        initialize(key, identifier, label, false);
    }

    public BooleanFilter(String key, String identifier, String label, boolean active) {
        initialize(key, identifier, label, active);
    }

    private void initialize(String key, String identifier, String label, boolean active) {
        this.key = key;
        this.identifier = identifier;
        this.active = active;
        this.label = label;
    }

    @Override
    public void updateFromView(View view) {
        this.active = ((ToggleButton) view.findViewById(R.id.active)).isChecked();
    }

    @Override
    public boolean isActive() {
        return this.active;
    }

    @Override
    public void clear() {
        active = false;
    }

    @Override
    public JSONObject getFilterObject() {
        return buildNestedFilter(this.identifier, "IS", this.active);
    }
}
