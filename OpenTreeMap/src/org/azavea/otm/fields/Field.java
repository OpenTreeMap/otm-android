package org.azavea.otm.fields;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.azavea.otm.App;
import org.azavea.otm.NestedJsonAndKey;
import org.azavea.otm.R;
import org.azavea.otm.data.Model;
import org.azavea.otm.data.PendingEdit;
import org.azavea.otm.data.PendingEditDescription;
import org.azavea.otm.data.Plot;
import org.azavea.otm.ui.PendingItemDisplay;
import org.azavea.otm.ui.TreeInfoDisplay;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public abstract class Field {
    public static final String TREE_SPECIES = "tree.species";
    public static final String TREE_DIAMETER = "tree.diameter";

    public static final String DATE_TYPE = "date";
    public static final String CHOICE_TYPE = "choice";

    // This is the view control, either button or EditText, which has the user value
    protected View valueView = null;

    /**
     * The property name from Plot which will contain the data to display or
     * edit. Nested resources are separated by '.' notation
     */
    public final String key;

    /**
     * Label to identify the field on a view
     */
    public final String label;

    /**
     * Does the current user have permission to edit?
     */
    public final boolean canEdit;

    /**
     * How to format units
     */
    public final String format;

    public final String infoUrl;

    protected Field(JSONObject fieldDef) {
        key = fieldDef.optString("field_key");
        label = fieldDef.optString("display_name");
        canEdit = fieldDef.optBoolean("can_write");
        format = fieldDef.optString("data_type");

        // NOTE: Not enabled for OTM2 yet
        infoUrl = fieldDef.optString("info_url");
    }

    protected Field(String key, String label) {
        this.key = key;
        this.label = label;
        canEdit = false;
        format = null;
        infoUrl = null;
    }

    public static Field makeField(JSONObject fieldDef) {
        String format = fieldDef.optString("data_type");
        String key = fieldDef.optString("field_key");

        if (CHOICE_TYPE.equals(format)) {
            return new ChoiceField(fieldDef);
        } else if (DATE_TYPE.equals(format)) {
            return new DateField(fieldDef);
        } else if (TREE_SPECIES.equals(key)) {
            return new SpeciesField(fieldDef);
        } else if (TREE_DIAMETER.equals(key)) {
            return new DiameterField(fieldDef);
        } else {
            return new TextField(fieldDef);
        }
    }

    /**
     * Render a view to display the given model field in edit mode
     */
    public abstract View renderForEdit(LayoutInflater layout, Plot model, Activity activity);

    /**
     * Gets the edited value for use when updating
     *
     * @return The edited value - may be of any type
     */
    protected abstract Object getEditedValue();

    /*
     * Render a view to display the given model field in view mode
     */
    public View renderForDisplay(LayoutInflater layout, Plot model, Activity activity) throws JSONException {

        // our ui elements
        View container = layout.inflate(R.layout.plot_field_row, null);
        TextView label = (TextView) container.findViewById(R.id.field_label);
        TextView fieldValue = (TextView) container.findViewById(R.id.field_value);
        View infoButton = container.findViewById(R.id.info);
        View pendingButton = container.findViewById(R.id.pending);

        // set the label (simple)
        label.setText(this.label);

        // is this field pending (based on its own notion of pending.)
        Boolean pending = isKeyPending(this.key, model);

        // Determine the current value of the field and update the ui. (Based on current
        // value or value of simple pending edit
        String value;
        if (!pending) {
            value = formatUnitIfPresent(getValueForKey(this.key, model));
        } else {
            value = model.getValueForLatestPendingEdit(this.key);
        }
        fieldValue.setText(value);

        // If the key is pending, display the arrow UI, and set up its click handler
        //
        // Note that the semantics of the bindPendingEditClickHandler function take
        // a key into the pending edit array, and an optional related field.
        if (pending) {
            bindPendingEditClickHandler(pendingButton, this.key, model, activity);
            pendingButton.setVisibility(View.VISIBLE);
        }

        // If the field has a URL attached to it as an info description (IE for pests) display the link.
        if (!TextUtils.isEmpty(this.infoUrl)) {
            infoButton.setVisibility(View.VISIBLE);
            bindInfoButtonClickHandler(infoButton, this.infoUrl, activity);
        }

        return container;
    }

    public void update(Model model) throws Exception {
        // If there is no valueView, this field was not rendered for edit
        if (this.valueView != null) {
            Object currentValue = getEditedValue();

            // If the model doesn't have they key, add it. This creates
            // a tree when tree values are added to a plot with no tree
            Plot p = (Plot) model;
            if (key.split("[.]")[0].equals("tree") && !p.hasTree() && currentValue != null) {
                p.createTree();
            }

            setValueForKey(key, model.getData(), currentValue);
        }
    }

    public void receiveActivityResult(int resultCode, Intent data) {
        Log.w(App.LOG_TAG, "Received intent data for a field which doesn't start an activity.  Ignoring the intent result.");
    }

    private String formatUnitIfPresent(Object value) {
        // If there is no value, return an unspecified value
        if (JSONObject.NULL.equals(value) || value.equals("")) {
            return App.getAppInstance().getResources().getString(R.string.unspecified_field_value);
        }
        return formatValue(value);
    }

    /**
     * Format the value with any units, if provided in the definition
     */
    protected String formatValue(Object value) {
        return value.toString();
    }

    public static Object getValueForKey(String key, Plot plot) throws JSONException {
        PendingEditDescription pending = plot.getPendingEditForKey(key);
        if (pending != null) {
            return plot.getPendingEditForKey(key).getLatestValue();
        } else {
            return getValueForKey(key, plot.getData());
        }
    }

    public static boolean isKeyPending(String key, Plot plot) throws JSONException {
        PendingEditDescription pending = plot.getPendingEditForKey(key);
        return pending != null;
    }

    /**
     * Return the value of a key name, which can be nested using . notation. If
     * the key does not exist or the value of the key, it will return a null
     * value
     */
    public static Object getValueForKey(String key, JSONObject json) {
        try {
            String[] keys = key.split("\\.");
            NestedJsonAndKey found = getValueForKey(keys, 0, json, false);
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

    /**
     * Return value for keys, which could be nested as an array
     */
    private static NestedJsonAndKey getValueForKey(String[] keys, int index, JSONObject json, boolean createNodeIfEmpty)
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

    private void setValueForKey(String key, JSONObject json, Object value) throws Exception {
        try {
            String[] keys = key.split("\\.");
            NestedJsonAndKey found = getValueForKey(keys, 0, json, true);
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

    /*
     *
     * key : the index into the pending edit array (IE Species) related field:
     * the value to return. (IE Species Name)
     *
     * If related field is null, return the plain value for the field. (Example,
     * when key is DBH, we want the numeric value.)
     */
    private void bindPendingEditClickHandler(View b, final String key, final Plot model,
                                             final Context context) {
        b.setOnClickListener(v -> {
            // initialize the intent, and load it with some initial values
            Intent pendingItemDisplay = new Intent(context, PendingItemDisplay.class);
            pendingItemDisplay.putExtra("label", label);
            pendingItemDisplay.putExtra("currentValue", formatUnitIfPresent(getValueForKey(key, model.getData())));
            pendingItemDisplay.putExtra("key", key);

            // Now create an array of pending values, [{id: X, value: "42",
            // username: "sam"}, ...]
            PendingEditDescription pendingEditDescription;
            try {
                pendingEditDescription = model.getPendingEditForKey(key);
                List<PendingEdit> pendingEdits = pendingEditDescription.getPendingEdits();
                JSONArray serializedPendingEdits = new JSONArray();
                for (PendingEdit pendingEdit : pendingEdits) {
                    // The value is the plain pending edit's value, or the value of the PE's
                    // related field. (IE retrieve Species Name instead of a species ID.)
                    String value = formatUnitIfPresent(pendingEdit.getValue());

                    // Continue on loading all of the pending edit data into
                    // the serializedPendingEdit object
                    JSONObject serializedPendingEdit = new JSONObject();
                    serializedPendingEdit.put("id", pendingEdit.getId());
                    serializedPendingEdit.put("value", value);
                    serializedPendingEdit.put("username", pendingEdit.getUsername());
                    try {
                        serializedPendingEdit.put("date", pendingEdit.getSubmittedTime().toLocaleString());
                    } catch (Exception e) {
                        e.printStackTrace();
                        serializedPendingEdit.put("date", "");
                    }

                    // and then append this edit onto the rest of them.
                    serializedPendingEdits.put(serializedPendingEdit);

                }
                pendingItemDisplay.putExtra("pending", serializedPendingEdits.toString());

                // And start the target activity
                Activity a = (Activity) context;
                a.startActivityForResult(pendingItemDisplay, TreeInfoDisplay.EDIT_REQUEST);
            } catch (JSONException e1) {
                Toast.makeText(context, "Sorry, pending edits not available.", Toast.LENGTH_SHORT).show();
                e1.printStackTrace();
            }
        });
    }

    private void bindInfoButtonClickHandler(View infoButton, final String url, final Context context) {
        infoButton.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            Activity a = (Activity) context;
            a.startActivity(browserIntent);
        });
    }
}