package org.azavea.otm.fields;

import android.app.Activity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import org.azavea.otm.R;
import org.azavea.otm.data.Plot;
import org.json.JSONObject;

public class TextField extends Field {
    /**
     * The text to append to the value as a unit
     */
    public final String unitText;

    /**
     * Number of significant digits to round to
     */
    public final int digits;

    protected TextField(JSONObject fieldDef) {
        super(fieldDef);

        unitText = fieldDef.optString("units");
        digits = fieldDef.optInt("digits");
    }

    /*
     * Render a view to display the given model field in edit mode
     */
    @Override
    public View renderForEdit(LayoutInflater layout, Plot plot, Activity activity, ViewGroup parent) {

        View container = null;

        if (this.canEdit) {
            container = layout.inflate(R.layout.plot_field_edit_row, parent, false);
            Object value = plot.getValueForKey(this.key);

            ((TextView) container.findViewById(R.id.field_label)).setText(this.label);
            EditText edit = (EditText) container.findViewById(R.id.field_value);
            TextView unitLabel = ((TextView) container.findViewById(R.id.field_unit));

            String safeValue = (!JSONObject.NULL.equals(value)) ? value.toString() : "";
            edit.setText(safeValue);
            this.valueView = edit;
            unitLabel.setText(this.unitText);

            if (this.format != null) {
                setFieldKeyboard(edit);
            }
        }

        return container;
    }

    /**
     * Format the value with any units, if provided in the definition
     */
    @Override
    protected String formatValue(Object value) {
        String formatted = "float".equals(format) ? formatWithDigits(value, this.digits) : String.valueOf(value);
        return formatted + " " + this.unitText;
    }

    @Override
    protected Object getEditedValue() {
        if (this.valueView != null) {
            // For proper JSON encoding of types, we'll use the keyboard type
            // to cast the edited value to the desired Java type. Choice buttons
            // are assumed to always be strings

            EditText textbox = (EditText) valueView;
            String text = textbox.getText().toString();

            if (TextUtils.isEmpty((textbox.getText().toString()))) {
                return null;
            }

            int inputType = textbox.getInputType();

            if ((inputType & InputType.TYPE_CLASS_TEXT) == InputType.TYPE_CLASS_TEXT) {
                return text;

            } else if ((inputType & InputType.TYPE_NUMBER_FLAG_DECIMAL) == InputType.TYPE_NUMBER_FLAG_DECIMAL) {
                return Double.parseDouble(text);
            } else if ((inputType & InputType.TYPE_CLASS_NUMBER) == InputType.TYPE_CLASS_NUMBER) {
                return Integer.parseInt(text);
            }

            return text;
        }
        return null;
    }

    public static String formatWithDigits(Object value, int digits) {
        try { // attempt to round 'value'
            Double d = Double.parseDouble(value.toString());
            return String.format("%." + digits + "f", d);
        } catch (ClassCastException e) {
            return value.toString();
        }
    }

    protected void setFieldKeyboard(EditText edit) {
        if (this.format.equals("float")) {
            edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        } else if (this.format.equalsIgnoreCase("int")) {
            edit.setInputType(InputType.TYPE_CLASS_NUMBER);
        } else {
            edit.setInputType(InputType.TYPE_CLASS_TEXT);
        }
    }
}