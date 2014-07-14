package org.azavea.otm.fields;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.azavea.otm.App;
import org.azavea.otm.NestedJsonAndKey;
import org.azavea.otm.R;
import org.azavea.otm.data.Model;
import org.azavea.otm.data.PendingEdit;
import org.azavea.otm.data.PendingEditDescription;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.Species;
import org.azavea.otm.data.User;
import org.azavea.otm.ui.PendingItemDisplay;
import org.azavea.otm.ui.TreeInfoDisplay;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

public class Field {
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
    public String key;

    /**
     * Label to identify the field on a view
     */
    public String label;

    /**
     * Does the current user have permission to edit?
     */
    public boolean canEdit;

    /**
     * The text to append to the value as a unit
     */
    public String unitText;

    /**
     * Number of significant digits to round to
     */
    public int digits;

    /**
     * How to format units
     */
    public String format;

    public String infoUrl = null;

    protected Field(JSONObject fieldDef) {
        key = fieldDef.optString("field_key");
        label = fieldDef.optString("display_name");
        canEdit = fieldDef.optBoolean("can_write");
        format = fieldDef.optString("data_type");

        unitText = fieldDef.optString("units");
        digits = fieldDef.optInt("digits");

        // NOTE: Not enabled for OTM2 yet
        infoUrl = fieldDef.optString("info_url");
    }

    protected Field(String key, String label) {
        this.key = key;
        this.label = label;
    }

    public static Field makeField(JSONObject fieldDef) {
        String format = fieldDef.optString("data_type");
        if (CHOICE_TYPE.equals(format)) {
            return new ChoiceField(fieldDef);
        }
        return new Field(fieldDef);
    }

    /*
     * Render a view to display the given model field in view mode
     */
    public View renderForDisplay(LayoutInflater layout, Plot model, Context context) throws JSONException {

        // our ui elements
        View container = layout.inflate(R.layout.plot_field_row, null);
        TextView label = (TextView) container.findViewById(R.id.field_label);
        TextView fieldValue = (TextView) container.findViewById(R.id.field_value);
        View infoButton = container.findViewById(R.id.info);
        View pendingButton = container.findViewById(R.id.pending);

        if (key.equals(TREE_SPECIES)) {
            return renderSpeciesFields(layout, model, context, container, label, fieldValue);
        }

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
            bindPendingEditClickHandler(pendingButton, this.key, model, context);
            pendingButton.setVisibility(View.VISIBLE);
        }

        // If the field has a URL attached to it as an info description (IE for pests) display the link.
        if (!TextUtils.isEmpty(this.infoUrl)) {
            infoButton.setVisibility(View.VISIBLE);
            bindInfoButtonClickHandler(infoButton, this.infoUrl, context);
        }

        return container;
    }

    private View renderSpeciesFields(LayoutInflater layout, Plot model, Context context, View container,
                                     TextView label, TextView fieldValue) {

        // tree.species gets exploded to a double row with sci name and common name
        label.setText("Scientific Name");
        fieldValue.setText(formatUnitIfPresent(model.getScienticName()));

        // TODO: It would be much better if this LinearLayout was defined in XML
        LinearLayout doubleRow = new LinearLayout(context);
        doubleRow.setOrientation(LinearLayout.VERTICAL);
        doubleRow.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        View containerCommon = layout.inflate(R.layout.plot_field_row, null);
        TextView labelCommon = (TextView) containerCommon.findViewById(R.id.field_label);
        TextView fieldValueCommon = (TextView) containerCommon.findViewById(R.id.field_value);

        labelCommon.setText("Common Name");
        fieldValueCommon.setText(formatUnitIfPresent(model.getCommonName()));

        doubleRow.addView(container);
        doubleRow.addView(containerCommon);

        return doubleRow;
    }

    /*
     * Render a view to display the given model field in edit mode
     */
    public View renderForEdit(LayoutInflater layout, Plot model, User user, Context context) {

        View container = null;

        if (this.canEdit) {
            container = layout.inflate(R.layout.plot_field_edit_row, null);
            Object value = getValueForKey(this.key, model.getData());

            ((TextView) container.findViewById(R.id.field_label)).setText(this.label);
            EditText edit = (EditText) container.findViewById(R.id.field_value);
            Button choiceButton = (Button) container.findViewById(R.id.choice_select);
            TextView unitLabel = ((TextView) container.findViewById(R.id.field_unit));

            if (TREE_SPECIES.equals(key) || DATE_TYPE.equals(format)) {
                edit.setVisibility(View.GONE);
                unitLabel.setVisibility(View.GONE);
                choiceButton.setVisibility(View.VISIBLE);
                this.valueView = choiceButton;
                if (TREE_SPECIES.equals(key)) {
                    setupSpeciesField(choiceButton, value, model);
                } else {
                    setupDateField(choiceButton, value, context);
                }
            } else {
                String safeValue = (!JSONObject.NULL.equals(value)) ? value.toString() : "";
                edit.setVisibility(View.VISIBLE);
                choiceButton.setVisibility(View.GONE);
                edit.setText(safeValue);
                this.valueView = edit;
                unitLabel.setText(this.unitText);

                if (this.format != null) {
                    setFieldKeyboard(edit);
                }

                // Special case for tree diameter. Make a synced circumference
                // field
                if (this.key.equals(TREE_DIAMETER)) {
                    return makeDynamicDbhFields(layout, context, container);
                }
            }
        }

        return container;
    }

    /**
     * Explode tree.diameter to include a circumference field which can update
     * each other
     */
    private View makeDynamicDbhFields(LayoutInflater layout, Context context, View container) {
        container.setId(R.id.dynamic_dbh);
        View circ = layout.inflate(R.layout.plot_field_edit_row, null);

        circ.setId(R.id.dynamic_circumference);
        circ.findViewById(R.id.choice_select).setVisibility(View.GONE);
        ((TextView) circ.findViewById(R.id.field_label)).setText(R.string.circumference_label);
        setFieldKeyboard((EditText) circ.findViewById(R.id.field_value));
        ((TextView) container.findViewById(R.id.field_unit)).setText(this.unitText);

        LinearLayout dynamicDbh = new LinearLayout(context);
        dynamicDbh.setOrientation(LinearLayout.VERTICAL);
        dynamicDbh.addView(container);
        dynamicDbh.addView(circ);
        return dynamicDbh;
    }

    private void setupSpeciesField(Button choiceButton, Object value, Model model) {
        JSONObject json = model.getData();

        // species could either be truly null, or an actual but empty JSONObject {}
        if (!JSONObject.NULL.equals(value)) {
            // Set the button text to the common and sci name
            String sciName = (String) getValueForKey("tree.species.scientific_name", json);
            String commonName = (String) getValueForKey("tree.species.common_name", json);
            choiceButton.setText(commonName + "\n" + sciName);
            Species speciesValue = new Species();
            speciesValue.setData((JSONObject) value);
            this.setValue(speciesValue);
        } else {
            choiceButton.setText(R.string.unspecified_field_value);
        }
    }

    private void setupDateField(final Button choiceButton, final Object value, final Context context) {
        if (!JSONObject.NULL.equals(value)) {
            String timestamp = (String) value;
            final String formattedDate = formatTimestampForDisplay(timestamp);
            choiceButton.setText(formattedDate);
            choiceButton.setTag(R.id.choice_button_value_tag, timestamp);
        } else {
            choiceButton.setText(R.string.unspecified_field_value);
        }
        choiceButton.setOnClickListener(v -> {
            final String setTimestamp = (String) choiceButton.getTag(R.id.choice_button_value_tag);
            final Calendar cal = getCalendarForTimestamp(context, setTimestamp);
            new DatePickerDialog(context, (view, year, month, day) -> {
                final String updatedTimestamp = getTimestamp(context, year, month, day);
                final String displayDate = formatTimestampForDisplay(updatedTimestamp);

                choiceButton.setText(displayDate);
                choiceButton.setTag(R.id.choice_button_value_tag, updatedTimestamp);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });
    }

    private void setFieldKeyboard(EditText edit) {
        if (this.format.equals("float")) {
            edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else if (this.format.equalsIgnoreCase("int")) {
            edit.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            edit.setInputType(InputType.TYPE_CLASS_TEXT);
        }
    }

    private String formatUnitIfPresent(Object value) {
        // If there is no value, return an unspecified value
        if (JSONObject.NULL.equals(value) || value.equals("")) {
            return App.getAppInstance().getResources().getString(R.string.unspecified_field_value);
        }
        return formatUnit(value);
    }

    /**
     * Format the value with any units, if provided in the definition
     */
    protected String formatUnit(Object value) {
        if (format != null) {
            if (format.equals("float")) {
                return formatWithDigits(value, this.digits) + " " + this.unitText;
            } else if (format.equals(DATE_TYPE)) {
                return formatTimestampForDisplay((String) value);
            }
        }
        return value + " " + this.unitText;
    }

    public String formatWithDigits(Object value, int digits) {
        try { // attempt to round 'value'
            Double d = Double.parseDouble(value.toString());
            return String.format("%." + digits + "f", d);
        } catch (ClassCastException e) {
            return value.toString();
        }
    }

    private Calendar getCalendarForTimestamp(Context context, String setTimestamp) {
        final Calendar cal = new GregorianCalendar();
        final SimpleDateFormat timestampFormatter =
                new SimpleDateFormat(context.getString(R.string.server_date_format));

        if (setTimestamp != null) {

            try {
                cal.setTime(timestampFormatter.parse(setTimestamp));
            } catch (ParseException e) {
                Log.e(App.LOG_TAG, "Error parsing date stored on tag.", e);
            }
        }
        return cal;
    }

    private String getTimestamp(Context context, int year, int month, int day) {
        final SimpleDateFormat timestampFormatter =
                new SimpleDateFormat(context.getString(R.string.server_date_format));
        final Calendar updatedCal = new GregorianCalendar();
        updatedCal.set(Calendar.YEAR, year);
        updatedCal.set(Calendar.MONTH, month);
        updatedCal.set(Calendar.DAY_OF_MONTH, day);

        return timestampFormatter.format(updatedCal.getTime());
    }

    private String formatTimestampForDisplay(String timestamp) {
        final String displayPattern = App.getCurrentInstance().getShortDateFormat();
        final String serverPattern = App.getAppInstance().getString(R.string.server_date_format);

        final SimpleDateFormat timestampFormatter = new SimpleDateFormat(serverPattern);
        final SimpleDateFormat displayFormatter = new SimpleDateFormat(displayPattern);
        try {
            final Date date = timestampFormatter.parse(timestamp);
            return displayFormatter.format(date);
        } catch (ParseException e) {
            return App.getAppInstance().getResources().getString(R.string.unspecified_field_value);
        }
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

    protected Object getEditedValue() throws Exception {
        if (this.valueView != null) {
            // For proper JSON encoding of types, we'll use the keyboard type
            // to cast the edited value to the desired Java type. Choice buttons
            // are assumed to always be strings

            if (this.valueView instanceof EditText) {
                EditText text = (EditText) valueView;
                if (TextUtils.isEmpty((text.getText().toString()))) {
                    return null;
                }

                int inputType = text.getInputType();

                if ((inputType & InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT) {
                    return text.getText().toString();

                } else if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) == InputType.TYPE_NUMBER_FLAG_DECIMAL) {
                    return Double.parseDouble(text.getText().toString());

                } else if ((inputType & InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER) {
                    return Integer.parseInt(text.getText().toString());

                }

                return text.getText().toString();

            } else if (this.valueView instanceof Button) {
                Object choiceVal = this.valueView.getTag(R.id.choice_button_value_tag);

                if (JSONObject.NULL.equals(choiceVal) || TextUtils.isEmpty(choiceVal.toString())) {
                    return null;
                }

                return choiceVal;

            } else {
                throw new Exception("Unknown ValueView type for field editing");
            }
        }
        return null;

    }

    public void attachClickListener(OnClickListener speciesClickListener) {
        if (this.valueView != null) {
            this.valueView.setOnClickListener(speciesClickListener);
        }
    }

    /**
     * Manual setting of the field value from an external client. The only
     * current use case for this is setting the species value on a species
     * selector from the calling activity.
     */
    public void setValue(Object value) {
        if (key.equals(TREE_SPECIES)) {
            Species species = (Species) value;

            if (this.valueView != null) {

                Button speciesButton = (Button) this.valueView;

                if (species != null) {

                    speciesButton.setTag(R.id.choice_button_value_tag, species.getData());
                    String label = species.getCommonName() + "\n" + species.getScientificName();
                    speciesButton.setText(label);
                }
            }
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