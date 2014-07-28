package org.azavea.otm.fields;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.azavea.otm.App;
import org.azavea.otm.R;
import org.azavea.otm.data.Model;
import org.azavea.otm.data.Plot;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FieldGroup {

    private String title;
    protected Map<String, Field> fields = new LinkedHashMap<>();

    private enum DisplayMode {VIEW, EDIT}

    protected FieldGroup() {

    }

    public FieldGroup(String title) {
        this.title = title;
    }

    public FieldGroup(JSONObject groupDefinition,
                      Map<String, JSONObject> fieldDefinitions) throws JSONException {

        this.title = groupDefinition.optString("header");
        JSONArray fieldKeys = groupDefinition.getJSONArray("field_keys");

        for (int i = 0; i < fieldKeys.length(); i++) {
            String key = fieldKeys.getString(i);
            addField(key, fieldDefinitions.get(key));
        }
    }

    public void addFields(Map<String, Field> fields) {
        this.fields = fields;
    }

    public void addField(String key, JSONObject fieldDef) {
        if (fieldDef == null) {
            Log.w(App.LOG_TAG, "Missing field definition for display field: " + key);
            return;
        }

        this.fields.put(key, Field.makeField(fieldDef));
    }

    public void addField(Field field) {
        this.fields.put(field.key, field);
    }

    public String getTitle() {
        return title;
    }

    public Map<String, Field> getFields() {
        return fields;
    }

    private View render(LayoutInflater layout, Plot model, DisplayMode mode, Activity activity, ViewGroup parent) {

        View container = layout.inflate(R.layout.plot_field_group, parent, false);
        LinearLayout group = (LinearLayout) container.findViewById(R.id.field_group);
        View fieldView;
        int renderedFieldCount = 0;

        ((TextView) group.findViewById(R.id.group_name)).setText(this.title);

        if (this.title != null) {
            for (Field field : fields.values()) {
                try {
                    fieldView = null;
                    switch (mode) {
                        case VIEW:
                            fieldView = field.renderForDisplay(layout, model, activity, group);
                            break;
                        case EDIT:
                            fieldView = field.renderForEdit(layout, model, activity, group);
                            break;
                    }

                    if (fieldView != null) {
                        renderedFieldCount++;
                        group.addView(fieldView);
                    }

                } catch (JSONException e) {
                    Log.d(App.LOG_TAG, "Error rendering field '" + field.key + "' " + e.getMessage());
                }
            }
        }
        if (renderedFieldCount > 0) {
            return group;
        } else {
            return null;
        }
    }

    /**
     * Render a field group and its child fields for viewing
     */
    public View renderForDisplay(LayoutInflater layout, Plot model, Activity activity, ViewGroup parent) {
        return render(layout, model, DisplayMode.VIEW, activity, parent);
    }

    /**
     * Render a field group and its child fields for editing
     */
    public View renderForEdit(LayoutInflater layout, Plot model, Activity activity, ViewGroup parent) {
        return render(layout, model, DisplayMode.EDIT, activity, parent);
    }

    public void update(Model model) throws Exception {
        for (Field field : fields.values()) {
            field.update(model);
        }
    }

    public void receiveActivityResult(int resultCode, Intent data) {
        Set<String> keys = data.getExtras().keySet();
        for (String key: keys) {
            if (fields.containsKey(key)) {
                fields.get(key).receiveActivityResult(resultCode, data);
            }
        }
    }
}
