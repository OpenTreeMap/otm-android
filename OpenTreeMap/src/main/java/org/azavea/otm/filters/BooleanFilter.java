package org.azavea.otm.filters;

import org.azavea.otm.R;
import org.json.JSONObject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

public class BooleanFilter extends BaseFilter {
    public boolean active;

    public BooleanFilter(String key, String identifier, String label) {
        super(key, identifier, label);
        this.active = false;
    }

    @Override
    public void updateFromView(View view) {
        this.active = ((ToggleButton) view.findViewById(R.id.active)).isChecked();
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public View createView(LayoutInflater inflater, Activity activity) {
        View toggle = inflater.inflate(R.layout.filter_toggle_control, null);
        ((TextView) toggle.findViewById(R.id.filter_label)).setText(label);
        if (isActive()) {
            setToggle(toggle, true);
        }
        return toggle;
    }

    @Override
    public void clear(View view) {
        active = false;
        setToggle(view, false);
    }

    @Override
    public JSONObject getFilterObject() {
        return buildNestedFilter(this.identifier, "IS", this.active);
    }

    private void setToggle(View toggle, boolean checked) {
        ((ToggleButton) toggle.findViewById(R.id.active)).setChecked(checked);
    }
}
